package com.speislist.backend.shoppinglist.service;

import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListItemDTO;
import com.speislist.backend.shoppinglist.entity.ShoppingList;
import com.speislist.backend.shoppinglist.entity.ShoppingListItem;
import com.speislist.backend.shoppinglist.entity.UserShoppingList;
import com.speislist.backend.shoppinglist.entity.UserShoppingListId;
import com.speislist.backend.shoppinglist.exception.ShoppingListItemNotFoundException;
import com.speislist.backend.shoppinglist.exception.ShoppingListNotFoundException;
import com.speislist.backend.shoppinglist.repository.ShoppingListItemRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListItemServiceTest {

    @Mock
    private ShoppingListItemRepository shoppingListItemRepository;

    @Mock
    private ShoppingListService shoppingListService;

    @InjectMocks
    private ShoppingListItemService shoppingListItemService;

    private User testUser;
    private ShoppingList testShoppingList;
    private ShoppingListItem testItem;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUserName("testuser");

        testShoppingList = new ShoppingList();
        testShoppingList.setId(1L);
        testShoppingList.setName("Groceries");
        testShoppingList.setCreatedAt(Instant.now());

        UserShoppingList userShoppingList = new UserShoppingList();
        userShoppingList.setId(new UserShoppingListId(testUser.getId(), testShoppingList.getId()));
        userShoppingList.setUser(testUser);
        userShoppingList.setShoppingList(testShoppingList);
        testShoppingList.setUserShoppingLists(new HashSet<>(Set.of(userShoppingList)));

        testItem = new ShoppingListItem();
        testItem.setId(10L);
        testItem.setName("Milk");
        testItem.setQuantity(2);
        testItem.setIsCompleted(false);
        testItem.setShoppingList(testShoppingList);

        testShoppingList.setItems(List.of(testItem));
    }

    @Nested
    @DisplayName("createShoppingListItem")
    class CreateShoppingListItemTests {

        @Test
        @DisplayName("should create a new shopping list item")
        void shouldCreateShoppingListItem() {
            when(shoppingListService.getShoppingListEntityById(1L, "user-123")).thenReturn(testShoppingList);
            when(shoppingListItemRepository.save(any(ShoppingListItem.class))).thenAnswer(invocation -> {
                ShoppingListItem saved = invocation.getArgument(0);
                saved.setId(10L);
                return saved;
            });

            ShoppingListItemDTO result = shoppingListItemService.createShoppingListItem(1L, "Bread", 3, "user-123");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Bread");
            assertThat(result.getQuantity()).isEqualTo(3);
            assertThat(result.getIsCompleted()).isFalse();

            ArgumentCaptor<ShoppingListItem> captor = ArgumentCaptor.forClass(ShoppingListItem.class);
            verify(shoppingListItemRepository).save(captor.capture());
            ShoppingListItem savedItem = captor.getValue();
            assertThat(savedItem.getName()).isEqualTo("Bread");
            assertThat(savedItem.getQuantity()).isEqualTo(3);
            assertThat(savedItem.getShoppingList()).isEqualTo(testShoppingList);
        }

        @Test
        @DisplayName("should create item with null quantity")
        void shouldCreateItemWithNullQuantity() {
            when(shoppingListService.getShoppingListEntityById(1L, "user-123")).thenReturn(testShoppingList);
            when(shoppingListItemRepository.save(any(ShoppingListItem.class))).thenAnswer(invocation -> {
                ShoppingListItem saved = invocation.getArgument(0);
                saved.setId(10L);
                return saved;
            });

            ShoppingListItemDTO result = shoppingListItemService.createShoppingListItem(1L, "Eggs", null, "user-123");

            assertThat(result.getQuantity()).isNull();
        }

        @Test
        @DisplayName("should throw exception when user cannot access shopping list")
        void shouldThrowExceptionWhenUserCannotAccessList() {
            when(shoppingListService.getShoppingListEntityById(1L, "other-user"))
                    .thenThrow(new ShoppingListNotFoundException(1L));

            assertThatThrownBy(() -> shoppingListItemService.createShoppingListItem(1L, "Bread", 1, "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getShoppingListItems")
    class GetShoppingListItemsTests {

        @Test
        @DisplayName("should return all items for a shopping list")
        void shouldReturnAllItems() {
            ShoppingListDTO dto = ShoppingListDTO.builder()
                    .id(1L)
                    .name("Groceries")
                    .items(List.of(
                            ShoppingListItemDTO.builder().id(10L).name("Milk").quantity(2).isCompleted(false).build(),
                            ShoppingListItemDTO.builder().id(11L).name("Bread").quantity(1).isCompleted(true).build()))
                    .build();
            when(shoppingListService.getShoppingListById(1L, "user-123")).thenReturn(dto);

            List<ShoppingListItemDTO> result = shoppingListItemService.getShoppingListItems(1L, "user-123");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(ShoppingListItemDTO::getName).containsExactly("Milk", "Bread");
        }

        @Test
        @DisplayName("should return empty list when shopping list has no items")
        void shouldReturnEmptyListWhenNoItems() {
            ShoppingListDTO dto = ShoppingListDTO.builder()
                    .id(1L)
                    .name("Groceries")
                    .items(List.of())
                    .build();
            when(shoppingListService.getShoppingListById(1L, "user-123")).thenReturn(dto);

            List<ShoppingListItemDTO> result = shoppingListItemService.getShoppingListItems(1L, "user-123");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getShoppingListItemById")
    class GetShoppingListItemByIdTests {

        @Test
        @DisplayName("should return item when user has access")
        void shouldReturnItemWhenUserHasAccess() {
            when(shoppingListItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doNothing().when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "user-123");

            ShoppingListItemDTO result = shoppingListItemService.getShoppingListItemById(10L, "user-123");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getName()).isEqualTo("Milk");
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void shouldThrowExceptionWhenItemNotFound() {
            when(shoppingListItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shoppingListItemService.getShoppingListItemById(999L, "user-123"))
                    .isInstanceOf(ShoppingListItemNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("should throw exception when user cannot access shopping list")
        void shouldThrowExceptionWhenUserCannotAccess() {
            when(shoppingListItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doThrow(new ShoppingListNotFoundException(1L))
                    .when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "other-user");

            assertThatThrownBy(() -> shoppingListItemService.getShoppingListItemById(10L, "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateShoppingListItem")
    class UpdateShoppingListItemTests {

        @Test
        @DisplayName("should update all fields when provided")
        void shouldUpdateAllFieldsWhenProvided() {
            when(shoppingListItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doNothing().when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "user-123");
            when(shoppingListItemRepository.save(any(ShoppingListItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingListItemDTO result = shoppingListItemService.updateShoppingListItem(10L, "Updated Name", 5, true,
                    "user-123");

            assertThat(result.getName()).isEqualTo("Updated Name");
            assertThat(result.getQuantity()).isEqualTo(5);
            assertThat(result.getIsCompleted()).isTrue();
        }

        @Test
        @DisplayName("should update only name when other fields are null")
        void shouldUpdateOnlyName() {
            when(shoppingListItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doNothing().when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "user-123");
            when(shoppingListItemRepository.save(any(ShoppingListItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingListItemDTO result = shoppingListItemService.updateShoppingListItem(10L, "New Name", null, null,
                    "user-123");

            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getQuantity()).isEqualTo(2); // unchanged
            assertThat(result.getIsCompleted()).isFalse(); // unchanged
        }

        @Test
        @DisplayName("should update only quantity when other fields are null")
        void shouldUpdateOnlyQuantity() {
            when(shoppingListItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doNothing().when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "user-123");
            when(shoppingListItemRepository.save(any(ShoppingListItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingListItemDTO result = shoppingListItemService.updateShoppingListItem(10L, null, 10, null,
                    "user-123");

            assertThat(result.getName()).isEqualTo("Milk"); // unchanged
            assertThat(result.getQuantity()).isEqualTo(10);
            assertThat(result.getIsCompleted()).isFalse(); // unchanged
        }

        @Test
        @DisplayName("should update only isCompleted when other fields are null")
        void shouldUpdateOnlyIsCompleted() {
            when(shoppingListItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doNothing().when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "user-123");
            when(shoppingListItemRepository.save(any(ShoppingListItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingListItemDTO result = shoppingListItemService.updateShoppingListItem(10L, null, null, true,
                    "user-123");

            assertThat(result.getName()).isEqualTo("Milk"); // unchanged
            assertThat(result.getQuantity()).isEqualTo(2); // unchanged
            assertThat(result.getIsCompleted()).isTrue();
        }

        @Test
        @DisplayName("should throw exception when user cannot access")
        void shouldThrowExceptionWhenUserCannotAccess() {
            when(shoppingListItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doThrow(new ShoppingListNotFoundException(1L))
                    .when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "other-user");

            assertThatThrownBy(() -> shoppingListItemService.updateShoppingListItem(10L, "Name", 1, true, "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("replaceShoppingListItem")
    class ReplaceShoppingListItemTests {

        @Test
        @DisplayName("should replace all fields including null values")
        void shouldReplaceAllFieldsIncludingNull() {
            when(shoppingListItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doNothing().when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "user-123");
            when(shoppingListItemRepository.save(any(ShoppingListItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingListItemDTO result = shoppingListItemService.replaceShoppingListItem(10L, "Replaced Item", null,
                    null, "user-123");

            assertThat(result.getName()).isEqualTo("Replaced Item");
            assertThat(result.getQuantity()).isNull();
            assertThat(result.getIsCompleted()).isNull();
        }

        @Test
        @DisplayName("should replace all fields with new values")
        void shouldReplaceAllFieldsWithNewValues() {
            when(shoppingListItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doNothing().when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "user-123");
            when(shoppingListItemRepository.save(any(ShoppingListItem.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingListItemDTO result = shoppingListItemService.replaceShoppingListItem(10L, "New Name", 100, true,
                    "user-123");

            assertThat(result.getName()).isEqualTo("New Name");
            assertThat(result.getQuantity()).isEqualTo(100);
            assertThat(result.getIsCompleted()).isTrue();
        }
    }

    @Nested
    @DisplayName("deleteShoppingListItem")
    class DeleteShoppingListItemTests {

        @Test
        @DisplayName("should delete item when user has access")
        void shouldDeleteItemWhenUserHasAccess() {
            when(shoppingListItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doNothing().when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "user-123");

            shoppingListItemService.deleteShoppingListItem(10L, "user-123");

            verify(shoppingListItemRepository).delete(testItem);
        }

        @Test
        @DisplayName("should throw exception when item not found")
        void shouldThrowExceptionWhenItemNotFound() {
            when(shoppingListItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shoppingListItemService.deleteShoppingListItem(999L, "user-123"))
                    .isInstanceOf(ShoppingListItemNotFoundException.class);

            verify(shoppingListItemRepository, never()).delete(any());
        }

        @Test
        @DisplayName("should throw exception when user cannot access")
        void shouldThrowExceptionWhenUserCannotAccess() {
            when(shoppingListItemRepository.findById(10L)).thenReturn(Optional.of(testItem));
            doThrow(new ShoppingListNotFoundException(1L))
                    .when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "other-user");

            assertThatThrownBy(() -> shoppingListItemService.deleteShoppingListItem(10L, "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);

            verify(shoppingListItemRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("deleteShoppingListItems")
    class DeleteShoppingListItemsTests {

        @Test
        @DisplayName("should delete multiple items when user has access to all")
        void shouldDeleteMultipleItems() {
            ShoppingListItem item2 = new ShoppingListItem();
            item2.setId(11L);
            item2.setName("Bread");
            item2.setShoppingList(testShoppingList);

            List<Long> ids = List.of(10L, 11L);
            when(shoppingListItemRepository.findByIdIn(ids)).thenReturn(List.of(testItem, item2));
            doNothing().when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "user-123");

            shoppingListItemService.deleteShoppingListItems(ids, "user-123");

            verify(shoppingListItemRepository).deleteByIdIn(ids);
        }

        @Test
        @DisplayName("should throw exception when user cannot access any item")
        void shouldThrowExceptionWhenUserCannotAccessAnyItem() {
            List<Long> ids = List.of(10L);
            when(shoppingListItemRepository.findByIdIn(ids)).thenReturn(List.of(testItem));
            doThrow(new ShoppingListNotFoundException(1L))
                    .when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "other-user");

            assertThatThrownBy(() -> shoppingListItemService.deleteShoppingListItems(ids, "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);

            verify(shoppingListItemRepository, never()).deleteByIdIn(any());
        }

        @Test
        @DisplayName("should validate access for items from different shopping lists")
        void shouldValidateAccessForDifferentLists() {
            ShoppingList anotherList = new ShoppingList();
            anotherList.setId(2L);
            anotherList.setName("Another List");

            ShoppingListItem itemFromAnotherList = new ShoppingListItem();
            itemFromAnotherList.setId(20L);
            itemFromAnotherList.setName("Item from another list");
            itemFromAnotherList.setShoppingList(anotherList);

            List<Long> ids = List.of(10L, 20L);
            when(shoppingListItemRepository.findByIdIn(ids)).thenReturn(List.of(testItem, itemFromAnotherList));
            doNothing().when(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "user-123");
            doNothing().when(shoppingListService).validateUserCanAccessShoppingList(anotherList, "user-123");

            shoppingListItemService.deleteShoppingListItems(ids, "user-123");

            verify(shoppingListService).validateUserCanAccessShoppingList(testShoppingList, "user-123");
            verify(shoppingListService).validateUserCanAccessShoppingList(anotherList, "user-123");
            verify(shoppingListItemRepository).deleteByIdIn(ids);
        }
    }
}
