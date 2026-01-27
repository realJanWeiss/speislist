package com.speislist.backend.inventory.service;

import com.speislist.backend.inventory.dto.response.InventoryItemDTO;
import com.speislist.backend.inventory.entity.Inventory;
import com.speislist.backend.inventory.entity.InventoryItem;
import com.speislist.backend.inventory.entity.UserInventory;
import com.speislist.backend.inventory.entity.UserInventoryId;
import com.speislist.backend.inventory.exception.InventoryItemNotFoundException;
import com.speislist.backend.inventory.exception.InventoryNotFoundException;
import com.speislist.backend.inventory.repository.InventoryItemRepository;
import com.speislist.backend.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryItemServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryItemService inventoryItemService;

    private User testUser;
    private Inventory testInventory;
    private InventoryItem testItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUserName("testuser");

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setName("Pantry");
        testInventory.setCreatedAt(Instant.now());

        UserInventory userInventory = new UserInventory();
        userInventory.setId(new UserInventoryId(testUser.getId(), testInventory.getId()));
        userInventory.setUser(testUser);
        userInventory.setInventory(testInventory);
        testInventory.setUserInventories(new HashSet<>(Set.of(userInventory)));

        testItem = new InventoryItem();
        testItem.setId(10L);
        testItem.setName("Milk");
        testItem.setExpirationDate(LocalDate.of(2026, 2, 15));
        testItem.setInventory(testInventory);

        testInventory.setItems(List.of(testItem));
    }

    @Nested
    @DisplayName("createInventoryItem")
    class CreateInventoryItemTests {

        @Test
        @DisplayName("should create a new inventory item")
        void shouldCreateInventoryItem() {
            when(inventoryService.getInventoryEntityById(1L, "user-123")).thenReturn(testInventory);
            when(inventoryItemRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> {
                InventoryItem saved = invocation.getArgument(0);
                saved.setId(10L);
                return saved;
            });

            LocalDate expirationDate = LocalDate.of(2026, 3, 1);
            InventoryItemDTO result = inventoryItemService.createInventoryItem(1L, "Bread", expirationDate, "user-123");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Bread");
            assertThat(result.getExpirationDate()).isEqualTo(expirationDate);

            ArgumentCaptor<InventoryItem> captor = ArgumentCaptor.forClass(InventoryItem.class);
            verify(inventoryItemRepository).save(captor.capture());
            InventoryItem savedItem = captor.getValue();
            assertThat(savedItem.getName()).isEqualTo("Bread");
            assertThat(savedItem.getExpirationDate()).isEqualTo(expirationDate);
            assertThat(savedItem.getInventory()).isEqualTo(testInventory);
        }

        @Test
        @DisplayName("should create item with null expiration date")
        void shouldCreateItemWithNullExpirationDate() {
            when(inventoryService.getInventoryEntityById(1L, "user-123")).thenReturn(testInventory);
            when(inventoryItemRepository.save(any(InventoryItem.class))).thenAnswer(invocation -> {
                InventoryItem saved = invocation.getArgument(0);
                saved.setId(10L);
                return saved;
            });

            InventoryItemDTO result = inventoryItemService.createInventoryItem(1L, "Salt", null, "user-123");

            assertThat(result.getExpirationDate()).isNull();
        }

        @Test
        @DisplayName("should throw exception when user cannot access inventory")
        void shouldThrowExceptionWhenUserCannotAccessInventory() {
            when(inventoryService.getInventoryEntityById(1L, "other-user"))
                    .thenThrow(new InventoryNotFoundException(1L));

            assertThatThrownBy(() -> inventoryItemService.createInventoryItem(1L, "Bread", null, "other-user"))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateInventoryItem")
    class UpdateInventoryItemTests {

        @Test
        @DisplayName("should update all fields when provided")
        void shouldUpdateAllFieldsWhenProvided() {
            LocalDate newExpirationDate = LocalDate.of(2026, 5, 1);
            when(inventoryItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doNothing().when(inventoryService).validateUserCanAccessInventory(testInventory, "user-123");
            when(inventoryItemRepository.save(any(InventoryItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InventoryItemDTO result = inventoryItemService.patchInventoryItem(10L, "Updated Name", newExpirationDate,
                    "user-123");

            assertThat(result.getName()).isEqualTo("Updated Name");
            assertThat(result.getExpirationDate()).isEqualTo(newExpirationDate);
        }

        @Test
        @DisplayName("should update only name when expiration date is null")
        void shouldUpdateOnlyName() {
            when(inventoryItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doNothing().when(inventoryService).validateUserCanAccessInventory(testInventory, "user-123");
            when(inventoryItemRepository.save(any(InventoryItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InventoryItemDTO result = inventoryItemService.patchInventoryItem(10L, "New Name", null, "user-123");

            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getExpirationDate()).isEqualTo(LocalDate.of(2026, 2, 15)); // unchanged
        }

        @Test
        @DisplayName("should update only expiration date when name is null")
        void shouldUpdateOnlyExpirationDate() {
            LocalDate newExpirationDate = LocalDate.of(2026, 6, 1);
            when(inventoryItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doNothing().when(inventoryService).validateUserCanAccessInventory(testInventory, "user-123");
            when(inventoryItemRepository.save(any(InventoryItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InventoryItemDTO result = inventoryItemService.patchInventoryItem(10L, null, newExpirationDate,
                    "user-123");

            assertThat(result.getName()).isEqualTo("Milk"); // unchanged
            assertThat(result.getExpirationDate()).isEqualTo(newExpirationDate);
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void shouldThrowExceptionWhenItemNotFound() {
            when(inventoryItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryItemService.patchInventoryItem(999L, "Name", null, "user-123"))
                    .isInstanceOf(InventoryItemNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("should throw exception when user cannot access")
        void shouldThrowExceptionWhenUserCannotAccess() {
            when(inventoryItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doThrow(new InventoryNotFoundException(1L))
                    .when(inventoryService).validateUserCanAccessInventory(testInventory, "other-user");

            assertThatThrownBy(() -> inventoryItemService.patchInventoryItem(10L, "Name", null, "other-user"))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteInventoryItems")
    class DeleteInventoryItemsTests {

        @Test
        @DisplayName("should delete multiple items when user has access to all")
        void shouldDeleteMultipleItems() {
            InventoryItem item2 = new InventoryItem();
            item2.setId(11L);
            item2.setName("Bread");
            item2.setInventory(testInventory);

            List<Long> ids = List.of(10L, 11L);
            when(inventoryItemRepository.findByIdIn(ids)).thenReturn(List.of(testItem, item2));
            doNothing().when(inventoryService).validateUserCanAccessInventory(testInventory, "user-123");

            inventoryItemService.deleteInventoryItems(ids, "user-123");

            verify(inventoryItemRepository).deleteByIdIn(ids);
        }

        @Test
        @DisplayName("should throw exception when user cannot access any item")
        void shouldThrowExceptionWhenUserCannotAccessAnyItem() {
            List<Long> ids = List.of(10L);
            when(inventoryItemRepository.findByIdIn(ids)).thenReturn(List.of(testItem));
            doThrow(new InventoryNotFoundException(1L))
                    .when(inventoryService).validateUserCanAccessInventory(testInventory, "other-user");

            assertThatThrownBy(() -> inventoryItemService.deleteInventoryItems(ids, "other-user"))
                    .isInstanceOf(InventoryNotFoundException.class);

            verify(inventoryItemRepository, never()).deleteByIdIn(any());
        }

        @Test
        @DisplayName("should validate access for items from different inventories")
        void shouldValidateAccessForDifferentInventories() {
            Inventory anotherInventory = new Inventory();
            anotherInventory.setId(2L);
            anotherInventory.setName("Another Inventory");

            InventoryItem itemFromAnotherInventory = new InventoryItem();
            itemFromAnotherInventory.setId(20L);
            itemFromAnotherInventory.setName("Item from another inventory");
            itemFromAnotherInventory.setInventory(anotherInventory);

            List<Long> ids = List.of(10L, 20L);
            when(inventoryItemRepository.findByIdIn(ids)).thenReturn(List.of(testItem, itemFromAnotherInventory));
            doNothing().when(inventoryService).validateUserCanAccessInventory(testInventory, "user-123");
            doNothing().when(inventoryService).validateUserCanAccessInventory(anotherInventory, "user-123");

            inventoryItemService.deleteInventoryItems(ids, "user-123");

            verify(inventoryService).validateUserCanAccessInventory(testInventory, "user-123");
            verify(inventoryService).validateUserCanAccessInventory(anotherInventory, "user-123");
            verify(inventoryItemRepository).deleteByIdIn(ids);
        }

        @Test
        @DisplayName("should throw exception when some items do not exist")
        void shouldThrowExceptionWhenSomeItemsDoNotExist() {
            List<Long> ids = List.of(10L, 999L);
            // Only one item is found
            when(inventoryItemRepository.findByIdIn(ids)).thenReturn(List.of(testItem));

            assertThatThrownBy(() -> inventoryItemService.deleteInventoryItems(ids, "user-123"))
                    .isInstanceOf(InventoryItemNotFoundException.class)
                    .hasMessageContaining("999");

            verify(inventoryItemRepository, never()).deleteByIdIn(any());
        }

        @Test
        @DisplayName("should throw exception when no items exist")
        void shouldThrowExceptionWhenNoItemsExist() {
            List<Long> ids = List.of(888L, 999L);
            when(inventoryItemRepository.findByIdIn(ids)).thenReturn(List.of());

            assertThatThrownBy(() -> inventoryItemService.deleteInventoryItems(ids, "user-123"))
                    .isInstanceOf(InventoryItemNotFoundException.class);

            verify(inventoryItemRepository, never()).deleteByIdIn(any());
        }
    }
}
