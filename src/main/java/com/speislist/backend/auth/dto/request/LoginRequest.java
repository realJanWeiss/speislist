package com.speislist.backend.auth.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LoginRequest {
    @NotNull
    private String email;
    @NotNull
    private String password;
}
