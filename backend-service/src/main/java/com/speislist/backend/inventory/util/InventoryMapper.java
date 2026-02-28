package com.speislist.backend.inventory.util;

import com.speislist.backend.inventory.dto.response.InventoryDTO;
import com.speislist.backend.inventory.dto.response.InventoryItemDTO;
import com.speislist.backend.inventory.entity.Inventory;
import com.speislist.backend.inventory.entity.InventoryItem;
import com.speislist.backend.user.util.UserMapper;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class InventoryMapper {

    public static InventoryDTO toInventoryDTO(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .name(inventory.getName())
                .createdAt(inventory.getCreatedAt())
                .members(inventory.getUserInventories() != null
                        ? inventory.getUserInventories().stream()
                        .map(userInventory -> UserMapper.toUserDTO(userInventory.getUser()))
                        .toList()
                        : List.of())
                .items(inventory.getItems() != null
                        ? inventory.getItems().stream()
                        .map(InventoryMapper::toInventoryItemDTO).toList()
                        : List.of())
                .build();
    }

    public static InventoryItemDTO toInventoryItemDTO(InventoryItem inventoryItem) {
        return InventoryItemDTO.builder()
                .id(inventoryItem.getId())
                .name(inventoryItem.getName())
                .expirationDate(inventoryItem.getExpirationDate())
                .build();
    }
}
