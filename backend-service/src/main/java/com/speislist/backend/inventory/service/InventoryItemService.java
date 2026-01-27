package com.speislist.backend.inventory.service;

import com.speislist.backend.inventory.dto.response.InventoryItemDTO;
import com.speislist.backend.inventory.entity.InventoryItem;
import com.speislist.backend.inventory.exception.InventoryItemNotFoundException;
import com.speislist.backend.inventory.repository.InventoryItemRepository;
import com.speislist.backend.inventory.util.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class InventoryItemService {
    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryService inventoryService;

    @Transactional
    public InventoryItemDTO createInventoryItem(long inventoryId, String name, LocalDate expirationDate,
            String userId) {
        final var inventory = inventoryService.getInventoryEntityById(inventoryId, userId);
        final var item = new InventoryItem();
        item.setName(name);
        item.setExpirationDate(expirationDate);
        item.setInventory(inventory);
        return InventoryMapper.toInventoryItemDTO(inventoryItemRepository.save(item));
    }

    @Transactional
    public InventoryItemDTO updateInventoryItem(long id, String name, LocalDate expirationDate, String userId) {
        return performUpdateInventoryItem(id, userId, item -> {
            if (name != null)
                item.setName(name);
            if (expirationDate != null)
                item.setExpirationDate(expirationDate);
        });
    }

    private InventoryItem getInventoryItemEntityById(long id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new InventoryItemNotFoundException(id));
    }

    private InventoryItemDTO performUpdateInventoryItem(long id, String userId, Consumer<InventoryItem> changer) {
        final var item = getInventoryItemEntityById(id);
        inventoryService.validateUserCanAccessInventory(item.getInventory(), userId);
        changer.accept(item);
        return InventoryMapper.toInventoryItemDTO(inventoryItemRepository.save(item));
    }

    @Transactional
    public void deleteInventoryItems(List<Long> ids, String userId) {
        final var inventoryItems = inventoryItemRepository.findByIdIn(ids);
        inventoryItems.forEach(item -> inventoryService.validateUserCanAccessInventory(item.getInventory(), userId));
        inventoryItemRepository.deleteByIdIn(ids);
    }
}
