package com.speislist.backend.shoppinglist.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ReplaceShoppingListItemRequest {
    @NotEmpty
    private String name;
    private Integer quantity;
    private Boolean isCompleted;
}
