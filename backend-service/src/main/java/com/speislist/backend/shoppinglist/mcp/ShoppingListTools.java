package com.speislist.backend.shoppinglist.mcp;

import com.speislist.backend.security.JwtUserService;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListItemDTO;
import com.speislist.backend.shoppinglist.service.ShoppingListItemService;
import com.speislist.backend.shoppinglist.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingListTools {
    private final ShoppingListService shoppingListService;
    private final ShoppingListItemService shoppingListItemService;
    private final JwtUserService jwtUserService;

    @McpTool(name = "get-shopping-lists", description = "Retrieves all shopping lists for the user.")
    public List<ShoppingListDTO> getShoppingLists() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return shoppingListService.getShoppingListsByUser(authentication.getName());
    }

    @McpTool(name = "create-shopping-list", description = "Creates a new shopping list for the user.")
    public ShoppingListDTO createShoppingList(
            @McpToolParam(description = "Name for the shopping list", required = true) String name) {
        final var currentUser = jwtUserService.getCurrentUser();
        return shoppingListService.createShoppingList(name, currentUser.userId());
    }

    @McpTool(name = "add-shopping-list-item", description = "Adds an item to a shopping list.")
    public ShoppingListItemDTO addItemToList(
            @McpToolParam(description = "Id of the shopping list", required = true) Long listId,
            @McpToolParam(description = "Name of the item to add", required = true) String itemName,
            @McpToolParam(description = "Quantity of the item", required = false) Integer quantity) {
        final var currentUser = jwtUserService.getCurrentUser();
        return shoppingListItemService.createShoppingListItem(listId, itemName, quantity, currentUser.userId());
    }

    @McpTool(name = "update-shopping-list-item", description = "Updates an item in a shopping list.")
    public ShoppingListItemDTO updateItemInList(
            @McpToolParam(description = "Id of the item to update", required = true) Long itemId,
            @McpToolParam(description = "New name of the item", required = false) String itemName,
            @McpToolParam(description = "New quantity of the item", required = false) Integer quantity,
            @McpToolParam(description = "Is the item completed", required = false) Boolean isCompleted) {
        final var currentUser = jwtUserService.getCurrentUser();
        return shoppingListItemService.updateShoppingListItem(
                itemId, itemName, quantity, isCompleted, currentUser.userId());
    }

    @McpTool(name = "remove-shopping-list-items", description = "Removes items from a shopping list.")
    public void removeItemFromList(
            @McpToolParam(description = "Id of the items to remove", required = true) List<Long> itemIds) {
        final var currentUser = jwtUserService.getCurrentUser();
        shoppingListItemService.deleteShoppingListItems(itemIds, currentUser.userId());
    }

    @McpTool(name = "invite-user-to-shopping-list", description = "Invites a user to a shopping list.")
    public ShoppingListDTO inviteUserToList(
            @McpToolParam(description = "Id of the shopping list", required = true) Long listId,
            @McpToolParam(description = "User name of the user to invite", required = true) String userName) {
        final var currentUser = jwtUserService.getCurrentUser();
        return shoppingListService.addUserToShoppingList(listId, userName, currentUser.userId());
    }

    @McpTool(name = "leave-or-delete-shopping-list", description = "Removes the current user from a shopping list. If the user is the last member, the list is deleted.")
    public void leaveList(@McpToolParam(description = "Id of the shopping list", required = true) Long listId) {
        final var currentUser = jwtUserService.getCurrentUser();
        shoppingListService.leaveShoppingList(listId, currentUser.userId());
    }
}
