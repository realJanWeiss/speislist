package com.speislist.backend.shoppinglist.dto.request;

import lombok.Data;

@Data
public class CreateShoppingListItemRequest {
    private String name;
    private Integer quantity;
}
