package com.speislist.backend.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    @NotNull
    private String id;

    @NotEmpty
    private String userName;
}
