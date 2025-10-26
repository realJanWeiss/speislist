package com.speislist.backend.shoppinglist.controller;

import com.speislist.backend.auth.annotation.SecuredOperation;
import com.speislist.backend.shoppinglist.dto.request.CreateShoppingListItemRequest;
import com.speislist.backend.shoppinglist.dto.request.UpdateShoppingListItemRequest;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListItemDTO;
import com.speislist.backend.shoppinglist.service.ShoppingListItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-lists/{shoppingListId}/items")
@RequiredArgsConstructor
public class ShoppingListItemController {
    private final ShoppingListItemService shoppingListItemService;

    @SecuredOperation(summary = "Create a shopping list item")
    @PostMapping
    public ResponseEntity<ShoppingListItemDTO> createShoppingListItem(@PathVariable Long shoppingListId, @RequestBody CreateShoppingListItemRequest request) {
        final var item = shoppingListItemService.createShoppingListItem(shoppingListId, request.getName(), request.getQuantity());
        return ResponseEntity.ok(item);
    }

    @SecuredOperation(summary = "Get all items for a shopping list")
    @GetMapping
    public ResponseEntity<List<ShoppingListItemDTO>> getShoppingListItems(@PathVariable Long shoppingListId) {
        final var items = shoppingListItemService.getShoppingListItems(shoppingListId);
        return ResponseEntity.ok(items);
    }

    @SecuredOperation(summary = "Get a shopping list item by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ShoppingListItemDTO> getShoppingListItem(@PathVariable Long shoppingListId, @PathVariable Long id) {
        final var item = shoppingListItemService.getShoppingListItemById(id);
        return ResponseEntity.ok(item);
    }

    @SecuredOperation(summary = "Update a shopping list item")
    @PutMapping("/{id}")
    public ResponseEntity<ShoppingListItemDTO> updateShoppingListItem(@PathVariable Long shoppingListId, @PathVariable Long id, @RequestBody UpdateShoppingListItemRequest request) {
        final var item = shoppingListItemService.updateShoppingListItem(id, request.getName(), request.getQuantity(), request.getIsCompleted());
        return ResponseEntity.ok(item);
    }

    @SecuredOperation(summary = "Delete a shopping list item")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShoppingListItem(@PathVariable Long shoppingListId, @PathVariable Long id) {
        shoppingListItemService.deleteShoppingListItem(id);
        return ResponseEntity.noContent().build();
    }
}
