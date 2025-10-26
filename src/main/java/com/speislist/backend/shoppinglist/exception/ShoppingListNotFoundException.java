package com.speislist.backend.shoppinglist.exception;

public class ShoppingListNotFoundException extends RuntimeException {
    public ShoppingListNotFoundException(Long id) {
        super("Shopping list not found with id: " + id);
    }
}
