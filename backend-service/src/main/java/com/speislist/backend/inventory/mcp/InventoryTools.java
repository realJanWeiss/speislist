package com.speislist.backend.inventory.mcp;

import com.speislist.backend.inventory.dto.response.InventoryDTO;
import com.speislist.backend.inventory.dto.response.InventoryItemDTO;
import com.speislist.backend.inventory.service.InventoryItemService;
import com.speislist.backend.inventory.service.InventoryService;
import com.speislist.backend.security.JwtUserService;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryTools {
    private final InventoryService inventoryService;
    private final InventoryItemService inventoryItemService;
    private final JwtUserService jwtUserService;

    @McpTool(name = "get-inventories", description = "Retrieves all inventories for the user.")
    public List<InventoryDTO> getInventories() {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        return inventoryService.getInventoriesByUser(authentication.getName());
    }

    @McpTool(name = "create-inventory", description = "Creates a new inventory for the user.")
    public InventoryDTO createInventory(
            @McpToolParam(description = "Name for the inventory", required = true) String name) {
        final var currentUser = jwtUserService.getCurrentUser();
        return inventoryService.createInventory(name, currentUser.userId());
    }

    @McpTool(name = "add-inventory-item", description = "Adds an item to an inventory.")
    public InventoryItemDTO addItemToInventory(
            @McpToolParam(description = "Id of the inventory", required = true) Long inventoryId,
            @McpToolParam(description = "Name of the item to add", required = true) String itemName,
            @McpToolParam(description = "Expiration date of the item", required = false) LocalDate expirationDate) {
        final var currentUser = jwtUserService.getCurrentUser();
        return inventoryItemService.createInventoryItem(inventoryId, itemName, expirationDate, currentUser.userId());
    }

    @McpTool(name = "update-inventory-item", description = "Updates an item in an inventory.")
    public InventoryItemDTO updateItemInInventory(
            @McpToolParam(description = "Id of the item to update", required = true) Long inventoryItemId,
            @McpToolParam(description = "New name of the item", required = false) String inventoryItemName,
            @McpToolParam(description = "New expiration date of the item", required = false) LocalDate expirationDate,
            @McpToolParam(description = "Update should behave like a PUT action, where all fields are replaced.", required = false) Boolean putUpdate) {
        final var currentUser = jwtUserService.getCurrentUser();
        if (putUpdate != null && putUpdate) {
            return inventoryItemService.putInventoryItem(inventoryItemId, inventoryItemName, expirationDate,
                    currentUser.userId());
        }
        return inventoryItemService.patchInventoryItem(
                inventoryItemId, inventoryItemName, expirationDate, currentUser.userId());
    }

    @McpTool(name = "remove-inventory-items", description = "Removes items from an inventory.")
    public void removeItemFromInventory(
            @McpToolParam(description = "Id of the items to remove", required = true) List<Long> inventoryItemIds) {
        final var currentUser = jwtUserService.getCurrentUser();
        inventoryItemService.deleteInventoryItems(inventoryItemIds, currentUser.userId());
    }

    @McpTool(name = "invite-user-to-inventory", description = "Invites a user to an inventory.")
    public InventoryDTO inviteUserToInventory(
            @McpToolParam(description = "Id of the inventory", required = true) Long inventoryId,
            @McpToolParam(description = "User name of the user to invite", required = true) String userName) {
        final var currentUser = jwtUserService.getCurrentUser();
        return inventoryService.addUserToInventory(inventoryId, userName, currentUser.userId());
    }

    @McpTool(name = "leave-or-delete-inventory", description = "Removes the current user from an inventory. If the user is the last member, the inventory is deleted.")
    public void leaveInventory(@McpToolParam(description = "Id of the inventory", required = true) Long inventoryId) {
        final var currentUser = jwtUserService.getCurrentUser();
        inventoryService.leaveInventory(inventoryId, currentUser.userId());
    }
}
