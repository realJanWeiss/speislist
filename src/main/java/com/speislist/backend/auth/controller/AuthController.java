package com.speislist.backend.auth.controller;

import com.speislist.backend.auth.annotation.SecuredOperation;
import com.speislist.backend.auth.service.AuthService;
import com.speislist.backend.auth.service.JwtService;
import com.speislist.backend.auth.dto.response.LoginResponse;
import com.speislist.backend.auth.dto.request.LoginRequest;
import com.speislist.backend.auth.dto.request.RegisterRequest;
import com.speislist.backend.user.UserService;
import com.speislist.backend.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        final var userDTO = authService.registerUser(request);
        return createLoginResponse(userDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        final var userDTO = authService.authenticate(request.getEmail(), request.getPassword());
        return createLoginResponse(userDTO);
    }

    @GetMapping("/current")
    @SecuredOperation
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    private ResponseEntity<LoginResponse> createLoginResponse(UserDTO userDTO) {
        final var token = jwtService.generateToken(userDTO.getId());
        return ResponseEntity.ok(new LoginResponse(token, userDTO));
    }
}
