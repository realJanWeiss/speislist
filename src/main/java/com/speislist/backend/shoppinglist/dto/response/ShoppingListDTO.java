package com.speislist.backend.shoppinglist.dto.response;

import com.speislist.backend.user.dto.UserDTO;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ShoppingListDTO {
    private Long id;
    private String name;
    private Instant createdAt;
    private List<ShoppingListItemDTO> items;
    private List<UserDTO> members;
}
