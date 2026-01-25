package com.speislist.backend.auth.controller;

import com.speislist.backend.auth.annotation.SecuredOperation;
import com.speislist.backend.user.UserService;
import com.speislist.backend.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/current")
    @SecuredOperation
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getUserDTOById(jwt.getSubject()));
    }
}
