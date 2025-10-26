package com.speislist.backend.shoppinglist.util;

import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListItemDTO;
import com.speislist.backend.shoppinglist.entity.ShoppingList;
import com.speislist.backend.shoppinglist.entity.ShoppingListItem;

public class ShoppingListMapper {

    public static ShoppingListDTO toShoppingListDTO(ShoppingList shoppingList) {
        final var shoppingListDTO = new ShoppingListDTO();
        shoppingListDTO.setId(shoppingList.getId());
        shoppingListDTO.setName(shoppingList.getName());
        shoppingListDTO.setCreatedAt(shoppingList.getCreatedAt());
        shoppingListDTO.setItems(shoppingList.getItems().stream()
                .map(ShoppingListMapper::toShoppingListItemDTO)
                .toList());
        return shoppingListDTO;
    }

    public static ShoppingListItemDTO toShoppingListItemDTO(ShoppingListItem shoppingListItem) {
        final var shoppingListItemDTO = new ShoppingListItemDTO();
        shoppingListItemDTO.setId(shoppingListItem.getId());
        shoppingListItemDTO.setName(shoppingListItem.getName());
        shoppingListItemDTO.setQuantity(shoppingListItem.getQuantity());
        shoppingListItemDTO.setIsCompleted(shoppingListItem.getIsCompleted());
        return shoppingListItemDTO;
    }
}
