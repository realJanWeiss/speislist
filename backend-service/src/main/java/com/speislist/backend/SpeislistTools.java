package com.speislist.backend;

import com.speislist.backend.security.JwtUserService;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListItemDTO;
import com.speislist.backend.shoppinglist.service.ShoppingListItemService;
import com.speislist.backend.shoppinglist.service.ShoppingListService;
import com.speislist.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpeislistTools {
    private final UserService userService;
    private final ShoppingListService shoppingListService;
    private final ShoppingListItemService shoppingListItemService;
    private final JwtUserService jwtUserService;

    @McpTool(name = "get-lists", description = "Retrieves all lists for the user.")
    public List<ShoppingListDTO> getShoppingLists() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return shoppingListService.getShoppingListsByUser(authentication.getName());
    }

    @McpTool(name = "create-list", description = "Creates a new list for the user.")
    public ShoppingListDTO createShoppingList(
            @McpToolParam(description = "Name for the list", required = true) String name) {
        final var currentUser = jwtUserService.getCurrentUser();
        return shoppingListService.createShoppingList(name, currentUser.userId());
    }

    @McpTool(name = "add-item", description = "Adds an item to a list.")
    public ShoppingListItemDTO addItemToList(
            @McpToolParam(description = "Id of the list", required = true) Long listId,
            @McpToolParam(description = "Name of the item to add", required = true) String itemName,
            @McpToolParam(description = "Quantity of the item", required = false) Integer quantity) {
        final var currentUser = jwtUserService.getCurrentUser();
        return shoppingListItemService.createShoppingListItem(listId, itemName, quantity, currentUser.userId());
    }

    @McpTool(name = "update-item", description = "Updates an item in a list.")
    public ShoppingListItemDTO updateItemInList(
            @McpToolParam(description = "Id of the list", required = true) Long listId,
            @McpToolParam(description = "Id of the item to update", required = true) Long itemId,
            @McpToolParam(description = "New name of the item", required = false) String itemName,
            @McpToolParam(description = "New quantity of the item", required = false) Integer quantity,
            @McpToolParam(description = "Is the item completed", required = false) Boolean isCompleted) {
        final var currentUser = jwtUserService.getCurrentUser();
        return shoppingListItemService.updateShoppingListItem(
                listId, itemId, itemName, quantity, isCompleted, currentUser.userId());
    }

    @McpTool(name = "invite-user", description = "Invites a user to a list.")
    public ShoppingListDTO inviteUserToList(
            @McpToolParam(description = "Id of the list", required = true) Long listId,
            @McpToolParam(description = "User name of the user to invite", required = true) String userName) {
        final var currentUser = jwtUserService.getCurrentUser();
        return shoppingListService.addUserToShoppingList(listId, userName, currentUser.userId());
    }

    @McpTool(name = "leave-or-delete-list", description = "Removes the current user from a list. If the user is the last member, the list is deleted.")
    public void leaveList(@McpToolParam(description = "Id of the list", required = true) Long listId) {
        final var currentUser = jwtUserService.getCurrentUser();
        shoppingListService.leaveShoppingList(listId, currentUser.userId());
    }
}
