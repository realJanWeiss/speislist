package com.speislist.backend.inventory.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class InventoryItemDTO {
    private Long id;
    private String name;
    private LocalDate expirationDate;
}
