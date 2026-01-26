package com.speislist.backend.shoppinglist.controller;

import com.speislist.backend.auth.annotation.SecuredOperation;
import com.speislist.backend.shoppinglist.dto.request.CreateShoppingListItemRequest;
import com.speislist.backend.shoppinglist.dto.request.ReplaceShoppingListItemRequest;
import com.speislist.backend.shoppinglist.dto.request.UpdateShoppingListItemRequest;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListItemDTO;
import com.speislist.backend.shoppinglist.service.ShoppingListItemService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
                                                                      @AuthenticationPrincipal Jwt jwt) {
        final var item = shoppingListItemService.createShoppingListItem(shoppingListId, request.getName(),
                request.getQuantity(), jwt.getSubject());
        return ResponseEntity.ok(item);
    }

    @GetMapping
    @SecuredOperation(summary = "Get all items for a shopping list")
    public ResponseEntity<List<ShoppingListItemDTO>> getShoppingListItems(@PathVariable Long shoppingListId, @AuthenticationPrincipal Jwt jwt) {
        final var items = shoppingListItemService.getShoppingListItems(shoppingListId, jwt.getSubject());
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    @SecuredOperation(summary = "Get a shopping list item by ID")
    public ResponseEntity<ShoppingListItemDTO> getShoppingListItem(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        final var item = shoppingListItemService.getShoppingListItemById(id, jwt.getSubject());
        return ResponseEntity.ok(item);
    }

    @PatchMapping("/{id}")
    @SecuredOperation(summary = "Update a shopping list item")
    public ResponseEntity<ShoppingListItemDTO> patchShoppingListItem(@PathVariable Long id, @Valid @RequestBody UpdateShoppingListItemRequest request, @AuthenticationPrincipal Jwt jwt) {
        final var item = shoppingListItemService.updateShoppingListItem(id, request.getName(), request.getQuantity(), request.getIsCompleted(), jwt.getSubject());
        return ResponseEntity.ok(item);
    }

    @PutMapping("/{id}")
    @SecuredOperation(summary = "Update a shopping list item")
    public ResponseEntity<ShoppingListItemDTO> putShoppingListItem(@PathVariable Long id, @Valid @RequestBody ReplaceShoppingListItemRequest request, @AuthenticationPrincipal Jwt jwt) {
        final var item = shoppingListItemService.replaceShoppingListItem(id, request.getName(), request.getQuantity(), request.getIsCompleted(), jwt.getSubject());
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{id}")
    @SecuredOperation(summary = "Delete a shopping list item")
    @ApiResponse(responseCode = "204", description = "Shopping list item deleted successfully")
    public ResponseEntity<Void> deleteShoppingListItem(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        shoppingListItemService.deleteShoppingListItem(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
