package com.speislist.backend.user;

import com.speislist.backend.user.dto.UserDTO;
import com.speislist.backend.user.entity.User;
import com.speislist.backend.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUserName("testuser");
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() {
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            User result = userService.getUserById("user-123");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("user-123");
            assertThat(result.getUserName()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowExceptionWhenNotFound() {
            when(userRepository.findById("unknown-user")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById("unknown-user"))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("id")
                    .hasMessageContaining("unknown-user");
        }
    }

    @Nested
    @DisplayName("getUserByUserName")
    class GetUserByUserNameTests {

        @Test
        @DisplayName("should return user when found by username")
        void shouldReturnUserWhenFoundByUserName() {
            when(userRepository.findUserByUserName("testuser")).thenReturn(Optional.of(testUser));

            User result = userService.getUserByUserName("testuser");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("user-123");
            assertThat(result.getUserName()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when username not found")
        void shouldThrowExceptionWhenUsernameNotFound() {
            when(userRepository.findUserByUserName("unknownuser")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByUserName("unknownuser"))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("username")
                    .hasMessageContaining("unknownuser");
        }
    }

    @Nested
    @DisplayName("getUserDTOById")
    class GetUserDTOByIdTests {

        @Test
        @DisplayName("should return UserDTO when user found")
        void shouldReturnUserDTOWhenFound() {
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

            UserDTO result = userService.getUserDTOById("user-123");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("user-123");
            assertThat(result.getUserName()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowExceptionWhenNotFound() {
            when(userRepository.findById("unknown-user")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserDTOById("unknown-user"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("upsertFromIdentityProvider")
    class UpsertFromIdentityProviderTests {

        @Test
        @DisplayName("should create new user when not exists")
        void shouldCreateNewUserWhenNotExists() {
            when(userRepository.findById("new-user-456")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserDTO result = userService.upsertFromIdentityProvider("new-user-456", "newuser");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("new-user-456");
            assertThat(result.getUserName()).isEqualTo("newuser");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User savedUser = captor.getValue();
            assertThat(savedUser.getId()).isEqualTo("new-user-456");
            assertThat(savedUser.getUserName()).isEqualTo("newuser");
        }

        @Test
        @DisplayName("should return existing user when already exists with same username")
        void shouldReturnExistingUserWhenAlreadyExists() {
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserDTO result = userService.upsertFromIdentityProvider("user-123", "testuser");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("user-123");
            assertThat(result.getUserName()).isEqualTo("testuser");

            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should update username when user exists with different username")
        void shouldUpdateUsernameWhenUserExistsWithDifferentUsername() {
            when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserDTO result = userService.upsertFromIdentityProvider("user-123", "updatedusername");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("user-123");
            assertThat(result.getUserName()).isEqualTo("updatedusername");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User savedUser = captor.getValue();
            assertThat(savedUser.getId()).isEqualTo("user-123");
            assertThat(savedUser.getUserName()).isEqualTo("updatedusername");
        }
    }
}
