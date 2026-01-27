package com.speislist.backend.inventory.dto.response;

import com.speislist.backend.user.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class InventoryDTO {
    private Long id;
    private String name;
    private Instant createdAt;
    private List<InventoryItemDTO> items;
    private List<UserDTO> members;
}
