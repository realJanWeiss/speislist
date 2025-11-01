package com.speislist.backend.auth.dto.response;

import com.speislist.backend.user.dto.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Data
public class LoginResponse {
    @Schema(description = "JWT token for authenticated access", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @NotEmpty
    private String token;

    @NotEmpty
    private UserDTO user;
}
