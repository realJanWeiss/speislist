package com.speislist.backend.shoppinglist.dto.response;

import lombok.Data;

@Data
public class ShoppingListItemDTO {
    private Long id;
    private String name;
    private Integer quantity;
    private Boolean isCompleted;
}
