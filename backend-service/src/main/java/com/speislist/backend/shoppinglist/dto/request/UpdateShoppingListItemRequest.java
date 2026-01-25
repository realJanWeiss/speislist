package com.speislist.backend.shoppinglist.dto.request;

import lombok.Data;

@Data
public class UpdateShoppingListItemRequest {
    private String name;
    private Integer quantity;
    private Boolean isCompleted;
}
