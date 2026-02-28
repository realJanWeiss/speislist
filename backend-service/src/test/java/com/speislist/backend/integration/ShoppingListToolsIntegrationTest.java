package com.speislist.backend.integration;

import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListItemDTO;
import com.speislist.backend.shoppinglist.mcp.ShoppingListTools;
import com.speislist.backend.shoppinglist.repository.ShoppingListRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("integration")
@EnableAutoConfiguration(exclude = OAuth2ClientAutoConfiguration.class)
class ShoppingListToolsIntegrationTest {

    @TestConfiguration
    static class MockJwtDecoderConfig {
        @Bean
        @Primary
        public JwtDecoder jwtDecoder() {
            // Return a no-op decoder; we set the SecurityContext manually
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "mock")
                    .build();
        }
    }

    private static final String USER_ID = "user-1-uuid";
    private static final String USER_NAME = "alice";
    private static final String USER2_ID = "user-2-uuid";
    private static final String USER2_NAME = "bob";

    @Autowired
    private ShoppingListTools shoppingListTools;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        shoppingListRepository.deleteAll();
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
    @DisplayName("Complete shopping list user flow")
    class CompleteShoppingListFlow {

        @Test
        @DisplayName("should support the full lifecycle: create list, add items, update, complete, remove, invite, leave")
        void fullLifecycle() {
            // 1. Initially no shopping lists
            var lists = shoppingListTools.getShoppingLists();
            assertThat(lists).isEmpty();

            // 2. Create a shopping list
            ShoppingListDTO created = shoppingListTools.createShoppingList("Weekly Groceries");
            assertThat(created.getId()).isNotNull();
            assertThat(created.getName()).isEqualTo("Weekly Groceries");
            assertThat(created.getMembers()).hasSize(1);
            assertThat(created.getMembers().get(0).getUserName()).isEqualTo(USER_NAME);
            assertThat(created.getItems()).isEmpty();

            Long listId = created.getId();

            // 3. Verify it shows up in the list
            lists = shoppingListTools.getShoppingLists();
            assertThat(lists).hasSize(1);
            assertThat(lists.get(0).getId()).isEqualTo(listId);

            // 4. Add items
            ShoppingListItemDTO milk = shoppingListTools.addItemToList(listId, "Milk", 2);
            assertThat(milk.getId()).isNotNull();
            assertThat(milk.getName()).isEqualTo("Milk");
            assertThat(milk.getQuantity()).isEqualTo(2);
            assertThat(milk.getIsCompleted()).isFalse();

            ShoppingListItemDTO bread = shoppingListTools.addItemToList(listId, "Bread", null);
            assertThat(bread.getId()).isNotNull();
            assertThat(bread.getName()).isEqualTo("Bread");
            assertThat(bread.getQuantity()).isNull();

            // 5. Update an item (partial — change name and mark completed)
            ShoppingListItemDTO updatedMilk =
                    shoppingListTools.updateItemInList(milk.getId(), "Oat Milk", 3, true);
            assertThat(updatedMilk.getName()).isEqualTo("Oat Milk");
            assertThat(updatedMilk.getQuantity()).isEqualTo(3);
            assertThat(updatedMilk.getIsCompleted()).isTrue();

            // 6. Update with nulls should not overwrite (patch semantics)
            ShoppingListItemDTO patchedMilk =
                    shoppingListTools.updateItemInList(milk.getId(), null, null, null);
            assertThat(patchedMilk.getName()).isEqualTo("Oat Milk");
            assertThat(patchedMilk.getQuantity()).isEqualTo(3);
            assertThat(patchedMilk.getIsCompleted()).isTrue();

            // 7. Remove one item
            shoppingListTools.removeItemFromList(List.of(bread.getId()));

            // Verify only milk remains
            var listAfterRemoval = shoppingListTools.getShoppingLists();
            assertThat(listAfterRemoval.get(0).getItems()).hasSize(1);
            assertThat(listAfterRemoval.get(0).getItems().get(0).getName()).isEqualTo("Oat Milk");

            // 8. Invite a second user
            ShoppingListDTO afterInvite = shoppingListTools.inviteUserToList(listId, USER2_NAME);
            assertThat(afterInvite.getMembers()).hasSize(2);
            assertThat(afterInvite.getMembers().stream().map(m -> m.getUserName()))
                    .containsExactlyInAnyOrder(USER_NAME, USER2_NAME);

            // 9. Switch to user2 and verify they can see the list
            setSecurityContext(USER2_ID, USER2_NAME);
            var user2Lists = shoppingListTools.getShoppingLists();
            assertThat(user2Lists).hasSize(1);
            assertThat(user2Lists.get(0).getId()).isEqualTo(listId);

            // 10. User2 leaves the list
            shoppingListTools.leaveList(listId);
            var user2ListsAfterLeave = shoppingListTools.getShoppingLists();
            assertThat(user2ListsAfterLeave).isEmpty();

            // 11. Original user still has the list
            setSecurityContext(USER_ID, USER_NAME);
            var user1Lists = shoppingListTools.getShoppingLists();
            assertThat(user1Lists).hasSize(1);

            // 12. Last member leaves — list is deleted
            shoppingListTools.leaveList(listId);
            var finalLists = shoppingListTools.getShoppingLists();
            assertThat(finalLists).isEmpty();
        }
    }

    @Nested
    @DisplayName("Shopping list access control")
    class AccessControl {

        @Test
        @DisplayName("should prevent non-member from accessing another user's shopping list")
        void nonMemberCannotAccessList() {
            // User1 creates a list
            ShoppingListDTO created = shoppingListTools.createShoppingList("Private List");
            Long listId = created.getId();

            // Switch to user2 (not a member)
            setSecurityContext(USER2_ID, USER2_NAME);

            // User2 should not see user1's list
            var user2Lists = shoppingListTools.getShoppingLists();
            assertThat(user2Lists).isEmpty();

            // User2 trying to add an item should fail
            assertThatThrownBy(() -> shoppingListTools.addItemToList(listId, "Sneaky Item", 1))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Shopping list edge cases")
    class EdgeCases {

        @Test
        @DisplayName("should allow creating multiple shopping lists")
        void multipleShoppingLists() {
            shoppingListTools.createShoppingList("Groceries");
            shoppingListTools.createShoppingList("Hardware Store");
            shoppingListTools.createShoppingList("Party Supplies");

            var lists = shoppingListTools.getShoppingLists();
            assertThat(lists).hasSize(3);
            assertThat(lists.stream().map(ShoppingListDTO::getName))
                    .containsExactlyInAnyOrder("Groceries", "Hardware Store", "Party Supplies");
        }

        @Test
        @DisplayName("should handle removing multiple items at once")
        void removeMultipleItems() {
            ShoppingListDTO list = shoppingListTools.createShoppingList("Bulk Remove Test");
            Long listId = list.getId();

            ShoppingListItemDTO item1 = shoppingListTools.addItemToList(listId, "Item 1", 1);
            ShoppingListItemDTO item2 = shoppingListTools.addItemToList(listId, "Item 2", 2);
            ShoppingListItemDTO item3 = shoppingListTools.addItemToList(listId, "Item 3", 3);

            // Remove two at once
            shoppingListTools.removeItemFromList(List.of(item1.getId(), item3.getId()));

            var updatedList = shoppingListTools.getShoppingLists();
            assertThat(updatedList.get(0).getItems()).hasSize(1);
            assertThat(updatedList.get(0).getItems().get(0).getName()).isEqualTo("Item 2");
        }

        @Test
        @DisplayName("inviting an already-added user should be idempotent")
        void inviteSameUserTwice() {
            ShoppingListDTO list = shoppingListTools.createShoppingList("Idempotent Test");
            Long listId = list.getId();

            shoppingListTools.inviteUserToList(listId, USER2_NAME);
            ShoppingListDTO afterSecondInvite = shoppingListTools.inviteUserToList(listId, USER2_NAME);

            assertThat(afterSecondInvite.getMembers()).hasSize(2);
        }
    }
}
