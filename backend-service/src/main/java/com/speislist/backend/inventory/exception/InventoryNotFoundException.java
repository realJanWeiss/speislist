package com.speislist.backend.inventory.exception;

public class InventoryNotFoundException extends RuntimeException {
    public InventoryNotFoundException(long id) {
        super("Shopping list not found with id: " + id);
    }
}
