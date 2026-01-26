package com.speislist.backend.shoppinglist.service;

import com.speislist.backend.shoppinglist.dto.response.ShoppingListItemDTO;
import com.speislist.backend.shoppinglist.entity.ShoppingListItem;
import com.speislist.backend.shoppinglist.exception.ShoppingListItemNotFoundException;
import com.speislist.backend.shoppinglist.repository.ShoppingListItemRepository;
import com.speislist.backend.shoppinglist.util.ShoppingListMapper;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class ShoppingListItemService {
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final ShoppingListService shoppingListService;

    @Transactional
    public ShoppingListItemDTO createShoppingListItem(long shoppingListId, String name, Integer quantity, String userId) {
        final var shoppingList = shoppingListService.getShoppingEntityListById(shoppingListId, userId);
        final var item = new ShoppingListItem();
        item.setName(name);
        item.setQuantity(quantity);
        item.setIsCompleted(false);
        item.setShoppingList(shoppingList);
        return ShoppingListMapper.toShoppingListItemDTO(shoppingListItemRepository.save(item));
    }

    @Transactional
    public List<ShoppingListItemDTO> getShoppingListItems(long shoppingListId, String userId) {
        return shoppingListService.getShoppingListById(shoppingListId, userId).getItems();
    }

    private ShoppingListItem getShoppingListItemEntityById(long id) {
        return shoppingListItemRepository.findById(id)
                .orElseThrow(() -> new ShoppingListItemNotFoundException(id));
    }

    public ShoppingListItemDTO getShoppingListItemById(long id, String userId) {
        final var shoppingListItem = getShoppingListItemEntityById(id);
        shoppingListService.validateUserCanAccessShoppingList(shoppingListItem.getShoppingList(), userId);
        return ShoppingListMapper.toShoppingListItemDTO(shoppingListItem);
    }

    private ShoppingListItemDTO performUpdateShoppingListItem(long id, String userId, Consumer<ShoppingListItem> changer) {
        final var item = getShoppingListItemEntityById(id);
        shoppingListService.validateUserCanAccessShoppingList(item.getShoppingList(), userId);
        changer.accept(item);
        return ShoppingListMapper.toShoppingListItemDTO(shoppingListItemRepository.save(item));
    }

    @Transactional
    public ShoppingListItemDTO updateShoppingListItem(long id, String name, Integer quantity, Boolean isCompleted, String userId) {
        return performUpdateShoppingListItem(id, userId, item -> {
            if (name != null) item.setName(name);
            if (quantity != null) item.setQuantity(quantity);
            if (isCompleted != null) item.setIsCompleted(isCompleted);
        });
    }

    /**
     * null values will overwrite existing values
     */
    @Transactional
    public ShoppingListItemDTO replaceShoppingListItem(long id, @NotNull String name, Integer quantity, Boolean isCompleted, String userId) {
        return performUpdateShoppingListItem(id, userId, item -> {
            item.setName(name);
            item.setQuantity(quantity);
            item.setIsCompleted(isCompleted);
        });
    }

    @Transactional
    public void deleteShoppingListItem(long id, String userId) {
        final var shoppingListItem = getShoppingListItemEntityById(id);
        shoppingListService.validateUserCanAccessShoppingList(shoppingListItem.getShoppingList(), userId);
        shoppingListItemRepository.delete(shoppingListItem);
    }

    @Transactional
    public void deleteShoppingListItems(List<Long> ids, String userId) {
        final var shoppingListItems = shoppingListItemRepository.findByIdIn(ids);
        shoppingListItems.forEach(item -> shoppingListService.validateUserCanAccessShoppingList(item.getShoppingList(), userId));
        shoppingListItemRepository.deleteByIdIn(ids);
    }

    private void validateItemBelongsToShoppingList(ShoppingListItem item, long shoppingListId) {
        if (item.getShoppingList().getId() != shoppingListId) {
            throw new ShoppingListItemNotFoundException(item.getId());
        }
    }
}
