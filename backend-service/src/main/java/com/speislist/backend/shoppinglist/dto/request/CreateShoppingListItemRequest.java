package com.speislist.backend.shoppinglist.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CreateShoppingListItemRequest {
    @NotEmpty
    private String name;
    private Integer quantity;
}
