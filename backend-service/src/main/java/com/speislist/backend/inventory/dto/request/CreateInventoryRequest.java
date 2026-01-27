package com.speislist.backend.inventory.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateInventoryRequest {
    @NotEmpty
    private String name;
}
