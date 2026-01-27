package com.speislist.backend.shoppinglist.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShoppingListItemDTO {
    private Long id;
    private String name;
    private Integer quantity;
    private Boolean isCompleted;
}
