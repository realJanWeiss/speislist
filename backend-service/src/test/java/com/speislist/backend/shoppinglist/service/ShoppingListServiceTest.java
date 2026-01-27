package com.speislist.backend.shoppinglist.service;

import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
import com.speislist.backend.shoppinglist.entity.ShoppingList;
import com.speislist.backend.shoppinglist.entity.UserShoppingList;
import com.speislist.backend.shoppinglist.entity.UserShoppingListId;
import com.speislist.backend.shoppinglist.exception.ShoppingListNotFoundException;
import com.speislist.backend.shoppinglist.repository.ShoppingListRepository;
import com.speislist.backend.shoppinglist.repository.UserShoppingListRepository;
import com.speislist.backend.user.UserService;
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
class ShoppingListServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private UserShoppingListRepository userShoppingListRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private User testUser;
    private ShoppingList testShoppingList;
    private UserShoppingList testUserShoppingList;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUserName("testuser");

        testShoppingList = new ShoppingList();
        testShoppingList.setId(1L);
        testShoppingList.setName("Groceries");
        testShoppingList.setCreatedAt(Instant.now());
        testShoppingList.setItems(List.of());

        testUserShoppingList = new UserShoppingList();
        testUserShoppingList.setId(new UserShoppingListId(testUser.getId(), testShoppingList.getId()));
        testUserShoppingList.setUser(testUser);
        testUserShoppingList.setShoppingList(testShoppingList);

        testShoppingList.setUserShoppingLists(new HashSet<>(Set.of(testUserShoppingList)));
    }

    @Nested
    @DisplayName("createShoppingList")
    class CreateShoppingListTests {

        @Test
        @DisplayName("should create a new shopping list for the user")
        void shouldCreateShoppingListForUser() {
            when(userService.getUserById("user-123")).thenReturn(testUser);
            when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> {
                ShoppingList saved = invocation.getArgument(0);
                saved.setId(1L);
                saved.setCreatedAt(Instant.now());
                return saved;
            });

            ShoppingListDTO result = shoppingListService.createShoppingList("Groceries", "user-123");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Groceries");
            assertThat(result.getId()).isEqualTo(1L);

            ArgumentCaptor<ShoppingList> captor = ArgumentCaptor.forClass(ShoppingList.class);
            verify(shoppingListRepository).save(captor.capture());
            ShoppingList savedList = captor.getValue();
            assertThat(savedList.getName()).isEqualTo("Groceries");
            assertThat(savedList.getUserShoppingLists()).hasSize(1);
        }

        @Test
        @DisplayName("should associate the user with the shopping list")
        void shouldAssociateUserWithShoppingList() {
            when(userService.getUserById("user-123")).thenReturn(testUser);
            when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(invocation -> {
                ShoppingList saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            shoppingListService.createShoppingList("Groceries", "user-123");

            ArgumentCaptor<ShoppingList> captor = ArgumentCaptor.forClass(ShoppingList.class);
            verify(shoppingListRepository).save(captor.capture());
            ShoppingList savedList = captor.getValue();

            assertThat(savedList.getUserShoppingLists())
                    .extracting(usl -> usl.getUser().getId())
                    .containsExactly("user-123");
        }
    }

    @Nested
    @DisplayName("getShoppingListsByUser")
    class GetShoppingListsByUserTests {

        @Test
        @DisplayName("should return all shopping lists for a user")
        void shouldReturnAllShoppingListsForUser() {
            ShoppingList list1 = createShoppingList(1L, "List 1");
            ShoppingList list2 = createShoppingList(2L, "List 2");
            when(shoppingListRepository.findByUserId("user-123")).thenReturn(List.of(list1, list2));

            List<ShoppingListDTO> result = shoppingListService.getShoppingListsByUser("user-123");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(ShoppingListDTO::getName).containsExactly("List 1", "List 2");
        }

        @Test
        @DisplayName("should return empty list when user has no shopping lists")
        void shouldReturnEmptyListWhenNoShoppingLists() {
            when(shoppingListRepository.findByUserId("user-123")).thenReturn(List.of());

            List<ShoppingListDTO> result = shoppingListService.getShoppingListsByUser("user-123");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getShoppingListById")
    class GetShoppingListByIdTests {

        @Test
        @DisplayName("should return shopping list when user is a member")
        void shouldReturnShoppingListWhenUserIsMember() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            ShoppingListDTO result = shoppingListService.getShoppingListById(1L, "user-123");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Groceries");
        }

        @Test
        @DisplayName("should throw exception when shopping list does not exist")
        void shouldThrowExceptionWhenShoppingListNotFound() {
            when(shoppingListRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shoppingListService.getShoppingListById(999L, "user-123"))
                    .isInstanceOf(ShoppingListNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("should throw exception when user is not a member")
        void shouldThrowExceptionWhenUserNotMember() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            assertThatThrownBy(() -> shoppingListService.getShoppingListById(1L, "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateShoppingList")
    class UpdateShoppingListTests {

        @Test
        @DisplayName("should update shopping list name")
        void shouldUpdateShoppingListName() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));
            when(shoppingListRepository.save(any(ShoppingList.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingListDTO result = shoppingListService.updateShoppingList(1L, "Updated Name", "user-123");

            assertThat(result.getName()).isEqualTo("Updated Name");
            verify(shoppingListRepository).save(testShoppingList);
        }

        @Test
        @DisplayName("should throw exception when user is not a member")
        void shouldThrowExceptionWhenUpdatingAsNonMember() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            assertThatThrownBy(() -> shoppingListService.updateShoppingList(1L, "Updated Name", "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteShoppingList")
    class DeleteShoppingListTests {

        @Test
        @DisplayName("should delete shopping list when user is a member")
        void shouldDeleteShoppingList() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            shoppingListService.deleteShoppingList(1L, "user-123");

            verify(shoppingListRepository).delete(testShoppingList);
        }

        @Test
        @DisplayName("should throw exception when user is not a member")
        void shouldThrowExceptionWhenDeletingAsNonMember() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            assertThatThrownBy(() -> shoppingListService.deleteShoppingList(1L, "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);

            verify(shoppingListRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("addUserToShoppingList")
    class AddUserToShoppingListTests {

        @Test
        @DisplayName("should add a new user to the shopping list")
        void shouldAddNewUserToShoppingList() {
            User newUser = new User();
            newUser.setId("new-user-456");
            newUser.setUserName("newuser");

            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));
            when(userService.getUserByUserName("newuser")).thenReturn(newUser);
            when(userShoppingListRepository.existsById(any(UserShoppingListId.class))).thenReturn(false);
            when(userShoppingListRepository.save(any(UserShoppingList.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ShoppingListDTO result = shoppingListService.addUserToShoppingList(1L, "newuser", "user-123");

            assertThat(result).isNotNull();

            ArgumentCaptor<UserShoppingList> captor = ArgumentCaptor.forClass(UserShoppingList.class);
            verify(userShoppingListRepository).save(captor.capture());
            UserShoppingList saved = captor.getValue();
            assertThat(saved.getUser().getId()).isEqualTo("new-user-456");
            assertThat(saved.getShoppingList().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should not add user if already a member")
        void shouldNotAddUserIfAlreadyMember() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));
            when(userService.getUserByUserName("testuser")).thenReturn(testUser);
            when(userShoppingListRepository.existsById(any(UserShoppingListId.class))).thenReturn(true);

            shoppingListService.addUserToShoppingList(1L, "testuser", "user-123");

            verify(userShoppingListRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when requesting user is not a member")
        void shouldThrowExceptionWhenAddingByNonMember() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            assertThatThrownBy(() -> shoppingListService.addUserToShoppingList(1L, "newuser", "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("removeUserFromShoppingList")
    class RemoveUserFromShoppingListTests {

        @Test
        @DisplayName("should remove user from shopping list")
        void shouldRemoveUserFromShoppingList() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            shoppingListService.removeUserFromShoppingList(1L, testUser, "user-123");

            verify(userShoppingListRepository).deleteById(new UserShoppingListId("user-123", 1L));
        }

        @Test
        @DisplayName("should throw exception when requesting user is not a member")
        void shouldThrowExceptionWhenRemovingByNonMember() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            assertThatThrownBy(() -> shoppingListService.removeUserFromShoppingList(1L, testUser, "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);

            verify(userShoppingListRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("leaveShoppingList")
    class LeaveShoppingListTests {

        @Test
        @DisplayName("should delete shopping list when user is the last member")
        void shouldDeleteShoppingListWhenLastMember() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            shoppingListService.leaveShoppingList(1L, "user-123");

            verify(shoppingListRepository).delete(testShoppingList);
            verify(userShoppingListRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should remove user association when other members exist")
        void shouldRemoveUserAssociationWhenOtherMembersExist() {
            User anotherUser = new User();
            anotherUser.setId("another-user");
            anotherUser.setUserName("anotheruser");

            UserShoppingList anotherUserShoppingList = new UserShoppingList();
            anotherUserShoppingList.setId(new UserShoppingListId(anotherUser.getId(), testShoppingList.getId()));
            anotherUserShoppingList.setUser(anotherUser);
            anotherUserShoppingList.setShoppingList(testShoppingList);

            testShoppingList.setUserShoppingLists(new HashSet<>(Set.of(testUserShoppingList, anotherUserShoppingList)));

            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            shoppingListService.leaveShoppingList(1L, "user-123");

            verify(userShoppingListRepository).deleteById(new UserShoppingListId("user-123", 1L));
            verify(shoppingListRepository, never()).delete(any());
        }

        @Test
        @DisplayName("should throw exception when user is not a member")
        void shouldThrowExceptionWhenLeavingAsNonMember() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            assertThatThrownBy(() -> shoppingListService.leaveShoppingList(1L, "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getShoppingListEntityById")
    class GetShoppingListEntityByIdTests {

        @Test
        @DisplayName("should return shopping list entity without user validation")
        void shouldReturnShoppingListEntityWithoutUserValidation() {
            when(shoppingListRepository.findById(1L)).thenReturn(Optional.of(testShoppingList));

            ShoppingList result = shoppingListService.getShoppingListEntityById(1L);

            assertThat(result).isEqualTo(testShoppingList);
        }

        @Test
        @DisplayName("should throw exception when shopping list not found")
        void shouldThrowExceptionWhenNotFound() {
            when(shoppingListRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> shoppingListService.getShoppingListEntityById(999L))
                    .isInstanceOf(ShoppingListNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("validateUserCanAccessShoppingList")
    class ValidateUserCanAccessShoppingListTests {

        @Test
        @DisplayName("should not throw when user is a member")
        void shouldNotThrowWhenUserIsMember() {
            shoppingListService.validateUserCanAccessShoppingList(testShoppingList, "user-123");
            // No exception means success
        }

        @Test
        @DisplayName("should throw when user is not a member")
        void shouldThrowWhenUserIsNotMember() {
            assertThatThrownBy(
                    () -> shoppingListService.validateUserCanAccessShoppingList(testShoppingList, "other-user"))
                    .isInstanceOf(ShoppingListNotFoundException.class);
        }
    }

    private ShoppingList createShoppingList(Long id, String name) {
        ShoppingList list = new ShoppingList();
        list.setId(id);
        list.setName(name);
        list.setCreatedAt(Instant.now());
        list.setItems(List.of());
        list.setUserShoppingLists(new HashSet<>(Set.of(testUserShoppingList)));
        return list;
    }
}
