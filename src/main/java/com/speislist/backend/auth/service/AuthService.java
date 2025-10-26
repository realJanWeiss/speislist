package com.speislist.backend.auth.service;

import com.speislist.backend.auth.dto.request.RegisterRequest;
import com.speislist.backend.auth.exception.InvalidCredentialsException;
import com.speislist.backend.user.UserRepository;
import com.speislist.backend.user.dto.UserDTO;
import com.speislist.backend.user.entity.User;
import com.speislist.backend.user.exception.UserAlreadyExistsException;
import com.speislist.backend.user.util.UserMapper;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserDTO registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new UserAlreadyExistsException(request.getEmail());
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return UserMapper.toUserDTO(userRepository.save(user));
    }

    public UserDTO authenticate(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        return UserMapper.toUserDTO(user);
    }
}