package com.speislist.backend.auth;

import com.speislist.backend.auth.dto.AuthResponse;
import com.speislist.backend.auth.dto.LoginRequest;
import com.speislist.backend.auth.dto.RegisterRequest;
import com.speislist.backend.user.User;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        final User user = authService.authenticate(request.getEmail(), request.getPassword());
        final String token = jwtService.generateToken(user.getId());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
