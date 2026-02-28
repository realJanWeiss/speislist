package com.speislist.backend.integration;

import com.speislist.backend.inventory.dto.response.InventoryDTO;
import com.speislist.backend.inventory.dto.response.InventoryItemDTO;
import com.speislist.backend.inventory.mcp.InventoryTools;
import com.speislist.backend.inventory.repository.InventoryRepository;
import com.speislist.backend.user.UserRepository;
import com.speislist.backend.user.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("integration")
@EnableAutoConfiguration(exclude = OAuth2ClientAutoConfiguration.class)
class InventoryToolsIntegrationTest {

    @TestConfiguration
    static class MockJwtDecoderConfig {
        @Bean
        @Primary
        public JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "mock")
                    .build();
        }
    }

    private static final String USER_ID = "inv-user-1-uuid";
    private static final String USER_NAME = "carol";
    private static final String USER2_ID = "inv-user-2-uuid";
    private static final String USER2_NAME = "dave";

    @Autowired
    private InventoryTools inventoryTools;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
        seedUser(USER_ID, USER_NAME);
        seedUser(USER2_ID, USER2_NAME);
        setSecurityContext(USER_ID, USER_NAME);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void seedUser(String id, String userName) {
        if (userRepository.findById(id).isEmpty()) {
            var user = new User();
            user.setId(id);
            user.setUserName(userName);
            userRepository.save(user);
        }
    }

    private void setSecurityContext(String userId, String userName) {
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .subject(userId)
                .claim("preferred_username", userName)
                .claim("email", userName + "@test.com")
                .claim("given_name", userName)
                .claim("family_name", "Test")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        var auth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Nested
    @DisplayName("Complete inventory user flow")
    class CompleteInventoryFlow {

        @Test
        @DisplayName("should support the full lifecycle: create inventory, add items, update (patch & put), remove, invite, leave")
        void fullLifecycle() {
            // 1. Initially no inventories
            var inventories = inventoryTools.getInventories();
            assertThat(inventories).isEmpty();

            // 2. Create an inventory
            InventoryDTO created = inventoryTools.createInventory("Kitchen Pantry");
            assertThat(created.getId()).isNotNull();
            assertThat(created.getName()).isEqualTo("Kitchen Pantry");
            assertThat(created.getMembers()).hasSize(1);
            assertThat(created.getMembers().get(0).getUserName()).isEqualTo(USER_NAME);
            assertThat(created.getItems()).isEmpty();

            Long inventoryId = created.getId();

            // 3. Verify it shows up in the list
            inventories = inventoryTools.getInventories();
            assertThat(inventories).hasSize(1);
            assertThat(inventories.get(0).getId()).isEqualTo(inventoryId);

            // 4. Add items
            LocalDate nextWeek = LocalDate.now().plusWeeks(1);
            InventoryItemDTO eggs = inventoryTools.addItemToInventory(inventoryId, "Eggs", nextWeek);
            assertThat(eggs.getId()).isNotNull();
            assertThat(eggs.getName()).isEqualTo("Eggs");
            assertThat(eggs.getExpirationDate()).isEqualTo(nextWeek);

            InventoryItemDTO rice = inventoryTools.addItemToInventory(inventoryId, "Rice", null);
            assertThat(rice.getId()).isNotNull();
            assertThat(rice.getName()).isEqualTo("Rice");
            assertThat(rice.getExpirationDate()).isNull();

            // 5. Patch update — only change name, leave expiration untouched
            LocalDate nextMonth = LocalDate.now().plusMonths(1);
            InventoryItemDTO patchedEggs =
                    inventoryTools.updateItemInInventory(eggs.getId(), "Free Range Eggs", null, null);
            assertThat(patchedEggs.getName()).isEqualTo("Free Range Eggs");
            assertThat(patchedEggs.getExpirationDate()).isEqualTo(nextWeek); // unchanged

            // 6. Patch update — only change expiration
            InventoryItemDTO patchedEggs2 =
                    inventoryTools.updateItemInInventory(eggs.getId(), null, nextMonth, null);
            assertThat(patchedEggs2.getName()).isEqualTo("Free Range Eggs"); // unchanged
            assertThat(patchedEggs2.getExpirationDate()).isEqualTo(nextMonth);

            // 7. Put update — replaces all fields (null clears expirationDate)
            InventoryItemDTO putEggs =
                    inventoryTools.updateItemInInventory(eggs.getId(), "Organic Eggs", null, true);
            assertThat(putEggs.getName()).isEqualTo("Organic Eggs");
            assertThat(putEggs.getExpirationDate()).isNull(); // cleared by PUT

            // 8. Remove one item
            inventoryTools.removeItemFromInventory(List.of(rice.getId()));

            var inventoryAfterRemoval = inventoryTools.getInventories();
            assertThat(inventoryAfterRemoval.get(0).getItems()).hasSize(1);
            assertThat(inventoryAfterRemoval.get(0).getItems().get(0).getName())
                    .isEqualTo("Organic Eggs");

            // 9. Invite a second user
            InventoryDTO afterInvite = inventoryTools.inviteUserToInventory(inventoryId, USER2_NAME);
            assertThat(afterInvite.getMembers()).hasSize(2);
            assertThat(afterInvite.getMembers().stream().map(m -> m.getUserName()))
                    .containsExactlyInAnyOrder(USER_NAME, USER2_NAME);

            // 10. Switch to user2 and verify they can see the inventory
            setSecurityContext(USER2_ID, USER2_NAME);
            var user2Inventories = inventoryTools.getInventories();
            assertThat(user2Inventories).hasSize(1);
            assertThat(user2Inventories.get(0).getId()).isEqualTo(inventoryId);

            // 11. User2 can add items
            InventoryItemDTO butter =
                    inventoryTools.addItemToInventory(inventoryId, "Butter", LocalDate.now().plusDays(14));
            assertThat(butter.getName()).isEqualTo("Butter");

            // 12. User2 leaves the inventory
            inventoryTools.leaveInventory(inventoryId);
            var user2InventoriesAfterLeave = inventoryTools.getInventories();
            assertThat(user2InventoriesAfterLeave).isEmpty();

            // 13. Original user still has the inventory with both items
            setSecurityContext(USER_ID, USER_NAME);
            var user1Inventories = inventoryTools.getInventories();
            assertThat(user1Inventories).hasSize(1);
            assertThat(user1Inventories.get(0).getItems()).hasSize(2);

            // 14. Last member leaves — inventory is deleted
            inventoryTools.leaveInventory(inventoryId);
            var finalInventories = inventoryTools.getInventories();
            assertThat(finalInventories).isEmpty();
        }
    }

    @Nested
    @DisplayName("Inventory access control")
    class AccessControl {

        @Test
        @DisplayName("should prevent non-member from accessing another user's inventory")
        void nonMemberCannotAccessInventory() {
            // User1 creates an inventory
            InventoryDTO created = inventoryTools.createInventory("Private Inventory");
            Long inventoryId = created.getId();

            // Switch to user2 (not a member)
            setSecurityContext(USER2_ID, USER2_NAME);

            // User2 should not see user1's inventory
            var user2Inventories = inventoryTools.getInventories();
            assertThat(user2Inventories).isEmpty();

            // User2 trying to add an item should fail
            assertThatThrownBy(() -> inventoryTools.addItemToInventory(inventoryId, "Sneaky Item", null))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Inventory edge cases")
    class EdgeCases {

        @Test
        @DisplayName("should allow creating multiple inventories")
        void multipleInventories() {
            inventoryTools.createInventory("Kitchen");
            inventoryTools.createInventory("Bathroom");
            inventoryTools.createInventory("Garage");

            var inventories = inventoryTools.getInventories();
            assertThat(inventories).hasSize(3);
            assertThat(inventories.stream().map(InventoryDTO::getName))
                    .containsExactlyInAnyOrder("Kitchen", "Bathroom", "Garage");
        }

        @Test
        @DisplayName("should handle removing multiple items at once")
        void removeMultipleItems() {
            InventoryDTO inv = inventoryTools.createInventory("Bulk Remove Test");
            Long invId = inv.getId();

            InventoryItemDTO item1 = inventoryTools.addItemToInventory(invId, "Item A", null);
            InventoryItemDTO item2 = inventoryTools.addItemToInventory(invId, "Item B", null);
            InventoryItemDTO item3 = inventoryTools.addItemToInventory(invId, "Item C", null);

            inventoryTools.removeItemFromInventory(List.of(item1.getId(), item3.getId()));

            var updatedInv = inventoryTools.getInventories();
            assertThat(updatedInv.get(0).getItems()).hasSize(1);
            assertThat(updatedInv.get(0).getItems().get(0).getName()).isEqualTo("Item B");
        }

        @Test
        @DisplayName("inviting an already-added user should be idempotent")
        void inviteSameUserTwice() {
            InventoryDTO inv = inventoryTools.createInventory("Idempotent Test");
            Long invId = inv.getId();

            inventoryTools.inviteUserToInventory(invId, USER2_NAME);
            InventoryDTO afterSecondInvite = inventoryTools.inviteUserToInventory(invId, USER2_NAME);

            assertThat(afterSecondInvite.getMembers()).hasSize(2);
        }
    }
}
