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

    private UserRepository userRepository;

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public UserDTO getUserDTOById(Long userId) {
        return UserMapper.toUserDTO(getUserById(userId));
    }
}
