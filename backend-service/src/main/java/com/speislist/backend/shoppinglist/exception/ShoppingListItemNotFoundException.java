package com.speislist.backend.shoppinglist.exception;

public class ShoppingListItemNotFoundException extends RuntimeException {
    public ShoppingListItemNotFoundException(Long id) {
        super("Shopping list item not found with id: " + id);
    }
}
