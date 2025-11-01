package com.speislist.backend.shoppinglist.controller;

import com.speislist.backend.auth.annotation.SecuredOperation;
import com.speislist.backend.shoppinglist.dto.request.CreateShoppingListItemRequest;
import com.speislist.backend.shoppinglist.dto.request.UpdateShoppingListItemRequest;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListItemDTO;
import com.speislist.backend.shoppinglist.service.ShoppingListItemService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping
    @SecuredOperation(summary = "Create a shopping list item")
    public ResponseEntity<ShoppingListItemDTO> createShoppingListItem(@PathVariable Long shoppingListId,
                                                                      @Valid @RequestBody CreateShoppingListItemRequest request,
                                                                      @AuthenticationPrincipal Long userId) {
        final var item = shoppingListItemService.createShoppingListItem(shoppingListId, request.getName(),
                request.getQuantity(), userId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    @SecuredOperation(summary = "Get all items for a shopping list")
    public ResponseEntity<List<ShoppingListItemDTO>> getShoppingListItems(@PathVariable Long shoppingListId, @AuthenticationPrincipal Long userId) {
        final var items = shoppingListItemService.getShoppingListItems(shoppingListId, userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    @SecuredOperation(summary = "Get a shopping list item by ID")
    public ResponseEntity<ShoppingListItemDTO> getShoppingListItem(@PathVariable Long shoppingListId, @PathVariable Long id) {
        final var item = shoppingListItemService.getShoppingListItemById(id);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{id}")
    @SecuredOperation(summary = "Update a shopping list item")
    public ResponseEntity<ShoppingListItemDTO> updateShoppingListItem(@PathVariable Long shoppingListId, @PathVariable Long id, @Valid @RequestBody UpdateShoppingListItemRequest request) {
        final var item = shoppingListItemService.updateShoppingListItem(id, request.getName(), request.getQuantity(), request.getIsCompleted());
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{id}")
    @SecuredOperation(summary = "Delete a shopping list item")
    @ApiResponse(responseCode = "204", description = "Shopping list item deleted successfully")
    public ResponseEntity<Void> deleteShoppingListItem(@PathVariable Long shoppingListId, @PathVariable Long id, @AuthenticationPrincipal Long userId) {
        shoppingListItemService.deleteShoppingListItem(shoppingListId, id, userId);
        return ResponseEntity.noContent().build();
    }
}
