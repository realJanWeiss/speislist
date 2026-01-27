package com.speislist.backend.inventory.exception;

public class InventoryItemNotFoundException extends RuntimeException {
    public InventoryItemNotFoundException(Long id) {
        super("Inventory item not found with id: " + id);
    }
}
