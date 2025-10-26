package com.speislist.backend.shoppinglist.controller;

import com.speislist.backend.auth.annotation.SecuredOperation;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
import com.speislist.backend.shoppinglist.service.ShoppingListService;
import com.speislist.backend.shoppinglist.dto.request.CreateShoppingListRequest;
import com.speislist.backend.shoppinglist.dto.request.UpdateShoppingListRequest;
import com.speislist.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {
    private final ShoppingListService shoppingListService;

    @SecuredOperation(summary = "Create a shopping list")
    @PostMapping
    public ResponseEntity<ShoppingListDTO> createShoppingList(@RequestBody CreateShoppingListRequest request, @AuthenticationPrincipal User user) {
        final var shoppingList = shoppingListService.createShoppingList(request.getName(), user);
        return ResponseEntity.ok(shoppingList);
    }

    @SecuredOperation(summary = "Get all shopping lists for the authenticated user")
    @GetMapping
    public ResponseEntity<List<ShoppingListDTO>> getShoppingLists(@AuthenticationPrincipal User user) {
        final var shoppingLists = shoppingListService.getShoppingListsByUser(user);
        return ResponseEntity.ok(shoppingLists);
    }

    @SecuredOperation(summary = "Get a shopping list by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ShoppingListDTO> getShoppingList(@PathVariable Long id) {
        final var shoppingList = shoppingListService.getShoppingListById(id);
        return ResponseEntity.ok(shoppingList);
    }

    @SecuredOperation(summary = "Update a shopping list")
    @PutMapping("/{id}")
    public ResponseEntity<ShoppingListDTO> updateShoppingList(@PathVariable Long id, @RequestBody UpdateShoppingListRequest request) {
        final var shoppingListDTO = shoppingListService.updateShoppingList(id, request.getName());
        return ResponseEntity.ok(shoppingListDTO);
    }

    @SecuredOperation(summary = "Delete a shopping list")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShoppingList(@PathVariable Long id) {
        shoppingListService.deleteShoppingList(id);
        return ResponseEntity.noContent().build();
    }
}
