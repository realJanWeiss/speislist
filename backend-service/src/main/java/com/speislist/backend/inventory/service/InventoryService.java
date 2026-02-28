package com.speislist.backend.inventory.service;

import com.speislist.backend.inventory.dto.response.InventoryDTO;
import com.speislist.backend.inventory.entity.Inventory;
import com.speislist.backend.inventory.entity.UserInventory;
import com.speislist.backend.inventory.entity.UserInventoryId;
import com.speislist.backend.inventory.exception.InventoryNotFoundException;
import com.speislist.backend.inventory.repository.InventoryRepository;
import com.speislist.backend.inventory.repository.UserInventoryRepository;
import com.speislist.backend.inventory.util.InventoryMapper;
import com.speislist.backend.user.UserService;
import com.speislist.backend.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final UserService userService;
    private final InventoryRepository inventoryRepository;
    private final UserInventoryRepository userInventoryRepository;

    @Transactional
    public InventoryDTO createInventory(String name, String userId) {
        final var user = userService.getUserById(userId);
        var inventory = new Inventory();
        inventory.setName(name);

        final var userInventory = new UserInventory();
        userInventory.setId(new UserInventoryId());
        userInventory.setUser(user);
        userInventory.setInventory(inventory);

        inventory.setUserInventories(Set.of(userInventory));

        inventory = inventoryRepository.save(inventory);
        return InventoryMapper.toInventoryDTO(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryDTO> getInventoriesByUser(String userId) {
        return inventoryRepository.findByUserId(userId).stream()
                .map(InventoryMapper::toInventoryDTO)
                .toList();
    }

    @Transactional
    public InventoryDTO getInventoryById(long id, String userId) {
        final var inventory = getInventoryEntityById(id, userId);
        return InventoryMapper.toInventoryDTO(inventory);
    }

    @Transactional
    public InventoryDTO updateInventory(long id, String name, String userId) {
        final var inventory = getInventoryEntityById(id, userId);
        inventory.setName(name);
        final var updatedInventory = inventoryRepository.save(inventory);
        return InventoryMapper.toInventoryDTO(updatedInventory);
    }

    @Transactional
    public void deleteInventory(long id, String userId) {
        final var inventory = getInventoryEntityById(id, userId);
        inventoryRepository.delete(inventory);
    }

    @Transactional
    public InventoryDTO addUserToInventory(long inventoryId, @NotNull String userName, String requestingUserId) {
        final var inventory = getInventoryEntityById(inventoryId, requestingUserId);
        final var user = userService.getUserByUserName(userName);
        final var id = new UserInventoryId(user.getId(), inventoryId);
        if (userInventoryRepository.existsById(id)) {
            return InventoryMapper.toInventoryDTO(inventory); // already added
        }
        final var userInventory = new UserInventory();
        userInventory.setId(id);
        userInventory.setUser(user);
        userInventory.setInventory(inventory);
        inventory.getUserInventories().add(userInventory);
        inventoryRepository.save(inventory);
        return InventoryMapper.toInventoryDTO(inventory);
    }

    @Transactional
    public void removeUserFromInventory(long inventoryId, @NotNull User user, String requestingUserId) {
        final var inventory = getInventoryEntityById(inventoryId, requestingUserId);
        inventory.getUserInventories().removeIf(ui -> ui.getUser().getId().equals(user.getId()));
    }

    @Transactional
    public void leaveInventory(long inventoryId, String userId) {
        final var inventory = getInventoryEntityById(inventoryId, userId);
        if (inventory.getUserInventories().size() == 1) {
            inventoryRepository.delete(inventory);
        } else {
            inventory.getUserInventories().removeIf(ui -> ui.getUser().getId().equals(userId));
        }
    }

    Inventory getInventoryEntityById(long id) {
        return inventoryRepository.findById(id).orElseThrow(() -> new InventoryNotFoundException(id));
    }

    /**
     * Get Inventory and validate user is a member of the inventory
     */
    Inventory getInventoryEntityById(long id, String userId) {
        final var inventory = getInventoryEntityById(id);
        validateUserCanAccessInventory(inventory, userId);
        return inventory;
    }

    private boolean isMemberOfInventory(@NotNull Inventory inventory, String userId) {
        return inventory.getUserInventories().stream()
                .anyMatch(userInventory -> userInventory.getUser().getId().equals(userId));
    }

    void validateUserCanAccessInventory(@NotNull Inventory inventory, String userId) {
        if (!isMemberOfInventory(inventory, userId)) {
            throw new InventoryNotFoundException(inventory.getId());
        }
    }
}
