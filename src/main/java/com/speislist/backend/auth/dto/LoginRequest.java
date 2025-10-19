package com.speislist.backend.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LoginRequest {
    @NotNull
    private String email;
    @NotNull
    private String password;
}
