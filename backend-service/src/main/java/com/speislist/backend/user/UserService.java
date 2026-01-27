package com.speislist.backend.user;

import com.speislist.backend.user.dto.UserDTO;
import com.speislist.backend.user.entity.User;
import com.speislist.backend.user.exception.UserNotFoundException;
import com.speislist.backend.user.util.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(String userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new UserNotFoundException(UserNotFoundException.UserReferenceType.ID, userId));
    }

    public User getUserByUserName(String userName) {
        return userRepository
                .findUserByUserName(userName)
                .orElseThrow(
                        () -> new UserNotFoundException(UserNotFoundException.UserReferenceType.USERNAME, userName));
    }

    public UserDTO getUserDTOById(String userId) {
        return UserMapper.toUserDTO(getUserById(userId));
    }

    public UserDTO upsertFromIdentityProvider(String userId, String userName) {
        final var user = userRepository.findById(userId).orElseGet(() -> {
            final var newUser = new User();
            newUser.setId(userId);
            return newUser;
        });
        user.setUserName(userName);
        final var savedUser = userRepository.save(user);
        return UserMapper.toUserDTO(savedUser);
    }
}
