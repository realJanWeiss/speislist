package com.speislist.backend;

import com.speislist.backend.security.JwtUserService;
import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
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
public class SpeislistTools {
    private final ShoppingListService shoppingListService;
    private final JwtUserService jwtUserService;

    @McpTool(name = "get-lists", description = "Retrieves all lists for the user.")
    public List<ShoppingListDTO> getShoppingLists() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return shoppingListService.getShoppingListsByUser(authentication.getName());
    }

    @McpTool(name = "create-list", description = "Creates a new list for the user.")
    public ShoppingListDTO createShoppingList(@McpToolParam(description = "Name for the shopping list", required = true) String name) {
        final var currentUser = jwtUserService.getCurrentUser();
        return shoppingListService.createShoppingList(name, currentUser.userId(), currentUser.email());
    }

    @McpTool(name = "get-list", description = "Retrieves the current list items.")
    public ShoppingListDTO getShoppingList(@McpToolParam(description = "Id of the shopping list", required = true) Integer id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return shoppingListService.getShoppingListById(id, authentication.getName());
    }
}
