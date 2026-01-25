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
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public UserDTO getUserDTOById(String userId) {
        return UserMapper.toUserDTO(getUserById(userId));
    }

    public User upsertFromIdentityProvider(String userId, String userName) {
        return userRepository.findById(userId).orElseGet(() -> {
            var user = new User();
            user.setId(userId);
            user.setUserName(userName);
            return userRepository.save(user);
        });
    }
}
