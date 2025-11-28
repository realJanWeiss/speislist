package com.speislist.backend;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Service;

@Service
public class SpeislistTools {

    @McpTool(name = "get-shopping-list", description = "Retrieves the current shopping list items.")
    public String getShoppingList() {
        return "Apfel, Brot, Milch";
    }
}
