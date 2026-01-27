package com.speislist.backend.inventory.service;

import com.speislist.backend.inventory.dto.response.InventoryDTO;
import com.speislist.backend.inventory.entity.Inventory;
import com.speislist.backend.inventory.entity.UserInventory;
import com.speislist.backend.inventory.entity.UserInventoryId;
import com.speislist.backend.inventory.exception.InventoryNotFoundException;
import com.speislist.backend.inventory.repository.InventoryRepository;
import com.speislist.backend.inventory.repository.UserInventoryRepository;
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
class InventoryServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private UserInventoryRepository userInventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private User testUser;
    private Inventory testInventory;
    private UserInventory testUserInventory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUserName("testuser");

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setName("Pantry");
        testInventory.setCreatedAt(Instant.now());
        testInventory.setItems(List.of());

        testUserInventory = new UserInventory();
        testUserInventory.setId(new UserInventoryId(testUser.getId(), testInventory.getId()));
        testUserInventory.setUser(testUser);
        testUserInventory.setInventory(testInventory);

        testInventory.setUserInventories(new HashSet<>(Set.of(testUserInventory)));
    }

    @Nested
    @DisplayName("createInventory")
    class CreateInventoryTests {

        @Test
        @DisplayName("should create a new inventory for the user")
        void shouldCreateInventoryForUser() {
            when(userService.getUserById("user-123")).thenReturn(testUser);
            when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
                Inventory saved = invocation.getArgument(0);
                saved.setId(1L);
                saved.setCreatedAt(Instant.now());
                return saved;
            });

            InventoryDTO result = inventoryService.createInventory("Pantry", "user-123");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Pantry");
            assertThat(result.getId()).isEqualTo(1L);

            ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
            verify(inventoryRepository).save(captor.capture());
            Inventory savedInventory = captor.getValue();
            assertThat(savedInventory.getName()).isEqualTo("Pantry");
            assertThat(savedInventory.getUserInventories()).hasSize(1);
        }

        @Test
        @DisplayName("should associate the user with the inventory")
        void shouldAssociateUserWithInventory() {
            when(userService.getUserById("user-123")).thenReturn(testUser);
            when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
                Inventory saved = invocation.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            inventoryService.createInventory("Pantry", "user-123");

            ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
            verify(inventoryRepository).save(captor.capture());
            Inventory savedInventory = captor.getValue();

            assertThat(savedInventory.getUserInventories())
                    .extracting(ui -> ui.getUser().getId())
                    .containsExactly("user-123");
        }
    }

    @Nested
    @DisplayName("getInventoriesByUser")
    class GetInventoriesByUserTests {

        @Test
        @DisplayName("should return all inventories for a user")
        void shouldReturnAllInventoriesForUser() {
            Inventory inv1 = createInventory(1L, "Inventory 1");
            Inventory inv2 = createInventory(2L, "Inventory 2");
            when(inventoryRepository.findByUserId("user-123")).thenReturn(List.of(inv1, inv2));

            List<InventoryDTO> result = inventoryService.getInventoriesByUser("user-123");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(InventoryDTO::getName).containsExactly("Inventory 1", "Inventory 2");
        }

        @Test
        @DisplayName("should return empty list when user has no inventories")
        void shouldReturnEmptyListWhenNoInventories() {
            when(inventoryRepository.findByUserId("user-123")).thenReturn(List.of());

            List<InventoryDTO> result = inventoryService.getInventoriesByUser("user-123");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getInventoryById")
    class GetInventoryByIdTests {

        @Test
        @DisplayName("should return inventory when user is a member")
        void shouldReturnInventoryWhenUserIsMember() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            InventoryDTO result = inventoryService.getInventoryById(1L, "user-123");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Pantry");
        }

        @Test
        @DisplayName("should throw exception when inventory does not exist")
        void shouldThrowExceptionWhenInventoryNotFound() {
            when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.getInventoryById(999L, "user-123"))
                    .isInstanceOf(InventoryNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("should throw exception when user is not a member")
        void shouldThrowExceptionWhenUserNotMember() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            assertThatThrownBy(() -> inventoryService.getInventoryById(1L, "other-user"))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateInventory")
    class UpdateInventoryTests {

        @Test
        @DisplayName("should update inventory name")
        void shouldUpdateInventoryName() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
            when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> invocation.getArgument(0));

            InventoryDTO result = inventoryService.updateInventory(1L, "Updated Name", "user-123");

            assertThat(result.getName()).isEqualTo("Updated Name");
            verify(inventoryRepository).save(testInventory);
        }

        @Test
        @DisplayName("should throw exception when user is not a member")
        void shouldThrowExceptionWhenUpdatingAsNonMember() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            assertThatThrownBy(() -> inventoryService.updateInventory(1L, "Updated Name", "other-user"))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteInventory")
    class DeleteInventoryTests {

        @Test
        @DisplayName("should delete inventory when user is a member")
        void shouldDeleteInventory() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            inventoryService.deleteInventory(1L, "user-123");

            verify(inventoryRepository).delete(testInventory);
        }

        @Test
        @DisplayName("should throw exception when user is not a member")
        void shouldThrowExceptionWhenDeletingAsNonMember() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            assertThatThrownBy(() -> inventoryService.deleteInventory(1L, "other-user"))
                    .isInstanceOf(InventoryNotFoundException.class);

            verify(inventoryRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("addUserToInventory")
    class AddUserToInventoryTests {

        @Test
        @DisplayName("should add a new user to the inventory")
        void shouldAddNewUserToInventory() {
            User newUser = new User();
            newUser.setId("new-user-456");
            newUser.setUserName("newuser");

            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
            when(userService.getUserByUserName("newuser")).thenReturn(newUser);
            when(userInventoryRepository.existsById(any(UserInventoryId.class))).thenReturn(false);
            when(userInventoryRepository.save(any(UserInventory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            InventoryDTO result = inventoryService.addUserToInventory(1L, "newuser", "user-123");

            assertThat(result).isNotNull();

            ArgumentCaptor<UserInventory> captor = ArgumentCaptor.forClass(UserInventory.class);
            verify(userInventoryRepository).save(captor.capture());
            UserInventory saved = captor.getValue();
            assertThat(saved.getUser().getId()).isEqualTo("new-user-456");
            assertThat(saved.getInventory().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should include new member in returned DTO")
        void shouldIncludeNewMemberInReturnedDTO() {
            User newUser = new User();
            newUser.setId("new-user-456");
            newUser.setUserName("newuser");

            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
            when(userService.getUserByUserName("newuser")).thenReturn(newUser);
            when(userInventoryRepository.existsById(any(UserInventoryId.class))).thenReturn(false);
            when(userInventoryRepository.save(any(UserInventory.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            inventoryService.addUserToInventory(1L, "newuser", "user-123");

            // Verify the inventory's userInventories collection was updated
            assertThat(testInventory.getUserInventories()).hasSize(2);
            assertThat(testInventory.getUserInventories())
                    .extracting(ui -> ui.getUser().getUserName())
                    .containsExactlyInAnyOrder("testuser", "newuser");
        }

        @Test
        @DisplayName("should not add user if already a member")
        void shouldNotAddUserIfAlreadyMember() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
            when(userService.getUserByUserName("testuser")).thenReturn(testUser);
            when(userInventoryRepository.existsById(any(UserInventoryId.class))).thenReturn(true);

            inventoryService.addUserToInventory(1L, "testuser", "user-123");

            verify(userInventoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when requesting user is not a member")
        void shouldThrowExceptionWhenAddingByNonMember() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            assertThatThrownBy(() -> inventoryService.addUserToInventory(1L, "newuser", "other-user"))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("removeUserFromInventory")
    class RemoveUserFromInventoryTests {

        @Test
        @DisplayName("should remove user from inventory")
        void shouldRemoveUserFromInventory() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            inventoryService.removeUserFromInventory(1L, testUser, "user-123");

            verify(userInventoryRepository).deleteById(new UserInventoryId("user-123", 1L));
        }

        @Test
        @DisplayName("should throw exception when requesting user is not a member")
        void shouldThrowExceptionWhenRemovingByNonMember() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            assertThatThrownBy(() -> inventoryService.removeUserFromInventory(1L, testUser, "other-user"))
                    .isInstanceOf(InventoryNotFoundException.class);

            verify(userInventoryRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("leaveInventory")
    class LeaveInventoryTests {

        @Test
        @DisplayName("should delete inventory when user is the last member")
        void shouldDeleteInventoryWhenLastMember() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            inventoryService.leaveInventory(1L, "user-123");

            verify(inventoryRepository).delete(testInventory);
            verify(userInventoryRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should remove user association when other members exist")
        void shouldRemoveUserAssociationWhenOtherMembersExist() {
            User anotherUser = new User();
            anotherUser.setId("another-user");
            anotherUser.setUserName("anotheruser");

            UserInventory anotherUserInventory = new UserInventory();
            anotherUserInventory.setId(new UserInventoryId(anotherUser.getId(), testInventory.getId()));
            anotherUserInventory.setUser(anotherUser);
            anotherUserInventory.setInventory(testInventory);

            testInventory.setUserInventories(new HashSet<>(Set.of(testUserInventory, anotherUserInventory)));

            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            inventoryService.leaveInventory(1L, "user-123");

            verify(userInventoryRepository).deleteById(new UserInventoryId("user-123", 1L));
            verify(inventoryRepository, never()).delete(any());
        }

        @Test
        @DisplayName("should throw exception when user is not a member")
        void shouldThrowExceptionWhenLeavingAsNonMember() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            assertThatThrownBy(() -> inventoryService.leaveInventory(1L, "other-user"))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getInventoryEntityById")
    class GetInventoryEntityByIdTests {

        @Test
        @DisplayName("should return inventory entity without user validation")
        void shouldReturnInventoryEntityWithoutUserValidation() {
            when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

            Inventory result = inventoryService.getInventoryEntityById(1L);

            assertThat(result).isEqualTo(testInventory);
        }

        @Test
        @DisplayName("should throw exception when inventory not found")
        void shouldThrowExceptionWhenNotFound() {
            when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> inventoryService.getInventoryEntityById(999L))
                    .isInstanceOf(InventoryNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("validateUserCanAccessInventory")
    class ValidateUserCanAccessInventoryTests {

        @Test
        @DisplayName("should not throw when user is a member")
        void shouldNotThrowWhenUserIsMember() {
            inventoryService.validateUserCanAccessInventory(testInventory, "user-123");
            // No exception means success
        }

        @Test
        @DisplayName("should throw when user is not a member")
        void shouldThrowWhenUserIsNotMember() {
            assertThatThrownBy(() -> inventoryService.validateUserCanAccessInventory(testInventory, "other-user"))
                    .isInstanceOf(InventoryNotFoundException.class);
        }
    }

    private Inventory createInventory(Long id, String name) {
        Inventory inventory = new Inventory();
        inventory.setId(id);
        inventory.setName(name);
        inventory.setCreatedAt(Instant.now());
        inventory.setItems(List.of());
        inventory.setUserInventories(new HashSet<>(Set.of(testUserInventory)));
        return inventory;
    }
}
