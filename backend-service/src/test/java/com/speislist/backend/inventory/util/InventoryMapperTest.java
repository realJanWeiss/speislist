package com.speislist.backend.inventory.util;

import com.speislist.backend.inventory.dto.response.InventoryDTO;
import com.speislist.backend.inventory.entity.Inventory;
import com.speislist.backend.inventory.entity.InventoryItem;
import com.speislist.backend.inventory.entity.UserInventory;
import com.speislist.backend.inventory.entity.UserInventoryId;
import com.speislist.backend.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryMapperTest {

    @Nested
    @DisplayName("toInventoryDTO")
    class ToInventoryDTOTests {

        @Test
        @DisplayName("should return empty list (not null) for members when userInventories is null")
        void shouldReturnEmptyListForMembersWhenNull() {
            // Bug: InventoryMapper returns null for members when userInventories is null,
            // unlike ShoppingListMapper which returns List.of(). This causes NPEs in callers.
            Inventory inventory = new Inventory();
            inventory.setId(1L);
            inventory.setName("Pantry");
            inventory.setCreatedAt(Instant.now());
            inventory.setUserInventories(null);
            inventory.setItems(List.of());

            InventoryDTO dto = InventoryMapper.toInventoryDTO(inventory);

            assertThat(dto.getMembers())
                    .as("members should be an empty list, not null, when userInventories is null")
                    .isNotNull()
                    .isEmpty();
        }

        @Test
        @DisplayName("should return empty list (not null) for items when items is null")
        void shouldReturnEmptyListForItemsWhenNull() {
            // Bug: InventoryMapper returns null for items when items is null,
            // unlike ShoppingListMapper which returns List.of(). This causes NPEs in callers.
            User user = new User();
            user.setId("user-123");
            user.setUserName("testuser");

            Inventory inventory = new Inventory();
            inventory.setId(1L);
            inventory.setName("Pantry");
            inventory.setCreatedAt(Instant.now());

            UserInventory userInventory = new UserInventory();
            userInventory.setId(new UserInventoryId(user.getId(), inventory.getId()));
            userInventory.setUser(user);
            userInventory.setInventory(inventory);
            inventory.setUserInventories(new HashSet<>(Set.of(userInventory)));
            inventory.setItems(null);

            InventoryDTO dto = InventoryMapper.toInventoryDTO(inventory);

            assertThat(dto.getItems())
                    .as("items should be an empty list, not null, when items is null")
                    .isNotNull()
                    .isEmpty();
        }

        @Test
        @DisplayName("should map members correctly when userInventories is not null")
        void shouldMapMembersCorrectly() {
            User user = new User();
            user.setId("user-123");
            user.setUserName("testuser");

            Inventory inventory = new Inventory();
            inventory.setId(1L);
            inventory.setName("Pantry");
            inventory.setCreatedAt(Instant.now());

            UserInventory userInventory = new UserInventory();
            userInventory.setId(new UserInventoryId(user.getId(), inventory.getId()));
            userInventory.setUser(user);
            userInventory.setInventory(inventory);
            inventory.setUserInventories(new HashSet<>(Set.of(userInventory)));
            inventory.setItems(List.of());

            InventoryDTO dto = InventoryMapper.toInventoryDTO(inventory);

            assertThat(dto.getMembers()).hasSize(1);
            assertThat(dto.getMembers().get(0).getId()).isEqualTo("user-123");
            assertThat(dto.getMembers().get(0).getUserName()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should map items correctly when items is not null")
        void shouldMapItemsCorrectly() {
            Inventory inventory = new Inventory();
            inventory.setId(1L);
            inventory.setName("Pantry");
            inventory.setCreatedAt(Instant.now());
            inventory.setUserInventories(new HashSet<>());

            InventoryItem item = new InventoryItem();
            item.setId(10L);
            item.setName("Milk");
            item.setExpirationDate(LocalDate.of(2026, 3, 1));
            item.setInventory(inventory);
            inventory.setItems(List.of(item));

            InventoryDTO dto = InventoryMapper.toInventoryDTO(inventory);

            assertThat(dto.getItems()).hasSize(1);
            assertThat(dto.getItems().get(0).getId()).isEqualTo(10L);
            assertThat(dto.getItems().get(0).getName()).isEqualTo("Milk");
            assertThat(dto.getItems().get(0).getExpirationDate()).isEqualTo(LocalDate.of(2026, 3, 1));
        }
    }
}
