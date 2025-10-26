package com.speislist.backend.user;

import com.speislist.backend.user.dto.UserDTO;
import com.speislist.backend.user.exception.UserNotFoundException;
import com.speislist.backend.user.util.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private UserRepository userRepository;

    public UserDTO getUserById(Long userId) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserMapper.toUserDTO(user);
    }
}
