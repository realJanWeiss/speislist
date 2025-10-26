package com.speislist.backend.auth.dto.response;

import com.speislist.backend.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserDTO user;
}
