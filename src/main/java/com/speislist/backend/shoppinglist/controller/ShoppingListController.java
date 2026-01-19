package com.speislist.backend.shoppinglist.controller;

import com.speislist.backend.auth.annotation.SecuredOperation;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
import com.speislist.backend.shoppinglist.service.ShoppingListService;
import com.speislist.backend.shoppinglist.dto.request.CreateShoppingListRequest;
import com.speislist.backend.shoppinglist.dto.request.UpdateShoppingListRequest;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-lists")
@RequiredArgsConstructor
public class ShoppingListController {
    private final ShoppingListService shoppingListService;

    @PostMapping
    @SecuredOperation(summary = "Create a shopping list")
    @ApiResponse(responseCode = "201", description = "Shopping list created successfully")
    public ResponseEntity<ShoppingListDTO> createShoppingList(@Valid @RequestBody CreateShoppingListRequest request, @AuthenticationPrincipal OidcUser oidcUser) {
        final var shoppingList = shoppingListService.createShoppingList(request.getName(), oidcUser.getSubject(), oidcUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingList);
    }

    @GetMapping
    @SecuredOperation(summary = "Get all shopping lists for the authenticated user")
    public ResponseEntity<List<ShoppingListDTO>> getShoppingLists(@AuthenticationPrincipal OidcUser oidcUser) {
        final var shoppingLists = shoppingListService.getShoppingListsByUser(oidcUser.getSubject());
        return ResponseEntity.ok(shoppingLists);
    }

    @GetMapping("/{id}")
    @SecuredOperation(summary = "Get a shopping list by ID")
    public ResponseEntity<ShoppingListDTO> getShoppingList(@PathVariable Long id, @AuthenticationPrincipal OidcUser oidcUser) {
        final var shoppingList = shoppingListService.getShoppingListById(id, oidcUser.getSubject());
        return ResponseEntity.ok(shoppingList);
    }

    @PutMapping("/{id}")
    @SecuredOperation(summary = "Update a shopping list")
    public ResponseEntity<ShoppingListDTO> updateShoppingList(@PathVariable Long id, @Valid @RequestBody UpdateShoppingListRequest request, @AuthenticationPrincipal OidcUser oidcUser) {
        final var shoppingListDTO = shoppingListService.updateShoppingList(id, request.getName(), oidcUser.getSubject());
        return ResponseEntity.ok(shoppingListDTO);
    }

    @DeleteMapping("/{id}")
    @SecuredOperation(summary = "Delete a shopping list")
    @ApiResponse(responseCode = "204", description = "Shopping list deleted successfully")
    public ResponseEntity<Void> deleteShoppingList(@PathVariable Long id, @AuthenticationPrincipal OidcUser oidcUser) {
        shoppingListService.deleteShoppingList(id, oidcUser.getSubject());
        return ResponseEntity.noContent().build();
    }
}
