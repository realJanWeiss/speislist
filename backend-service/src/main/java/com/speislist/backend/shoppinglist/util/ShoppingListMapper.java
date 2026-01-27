package com.speislist.backend.shoppinglist.util;

import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListItemDTO;
import com.speislist.backend.shoppinglist.entity.ShoppingList;
import com.speislist.backend.shoppinglist.entity.ShoppingListItem;
import com.speislist.backend.user.util.UserMapper;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ShoppingListMapper {

    public static ShoppingListDTO toShoppingListDTO(ShoppingList shoppingList) {
        return ShoppingListDTO.builder()
                .id(shoppingList.getId())
                .name(shoppingList.getName())
                .createdAt(shoppingList.getCreatedAt())
                .items(shoppingList.getItems() != null
                        ? shoppingList.getItems().stream()
                                .map(ShoppingListMapper::toShoppingListItemDTO)
                                .toList()
                        : List.of())
                .members(shoppingList.getUserShoppingLists() != null
                        ? shoppingList.getUserShoppingLists().stream()
                                .map(userShoppingList -> UserMapper.toUserDTO(userShoppingList.getUser()))
                                .toList()
                        : List.of())
                .build();
    }

    public static ShoppingListItemDTO toShoppingListItemDTO(ShoppingListItem shoppingListItem) {
        return ShoppingListItemDTO.builder()
                .id(shoppingListItem.getId())
                .name(shoppingListItem.getName())
                .quantity(shoppingListItem.getQuantity())
                .isCompleted(shoppingListItem.getIsCompleted())
                .build();
    }
}
