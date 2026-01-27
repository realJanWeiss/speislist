package com.speislist.backend.inventory.controller;

import com.speislist.backend.auth.annotation.SecuredOperation;
import com.speislist.backend.inventory.dto.request.CreateInventoryRequest;
import com.speislist.backend.inventory.dto.request.UpdateInventoryRequest;
import com.speislist.backend.inventory.dto.response.InventoryDTO;
import com.speislist.backend.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @PostMapping
    @SecuredOperation(summary = "Create a new inventory")
    @ApiResponse(responseCode = "201", description = "Inventory created successfully")
    public ResponseEntity<InventoryDTO> createInventory(@Valid @RequestBody CreateInventoryRequest request, @AuthenticationPrincipal Jwt jwt) {
        final var inventory = inventoryService.createInventory(request.getName(), jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(inventory);
    }

    @GetMapping
    @SecuredOperation(summary = "Get all inventories for the authenticated user")
    public ResponseEntity<List<InventoryDTO>> getInventories(@AuthenticationPrincipal Jwt jwt) {
        final var inventories = inventoryService.getInventoriesByUser(jwt.getSubject());
        return ResponseEntity.ok(inventories);
    }

    @GetMapping("/{id}")
    @SecuredOperation(summary = "Get a inventory by ID")
    public ResponseEntity<InventoryDTO> getInventory(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        final var inventory = inventoryService.getInventoryById(id, jwt.getSubject());
        return ResponseEntity.ok(inventory);
    }

    @PutMapping("/{id}")
    @SecuredOperation(summary = "Update an inventory")
    public ResponseEntity<InventoryDTO> updateInventory(@PathVariable Long id, @Valid @RequestBody UpdateInventoryRequest request, @AuthenticationPrincipal Jwt jwt) {
        final var inventoryDTO = inventoryService.updateInventory(id, request.getName(), jwt.getSubject());
        return ResponseEntity.ok(inventoryDTO);
    }

    @DeleteMapping("/{id}")
    @SecuredOperation(summary = "Delete an inventory")
    @ApiResponse(responseCode = "204", description = "Inventory deleted successfully")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        inventoryService.deleteInventory(id, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
