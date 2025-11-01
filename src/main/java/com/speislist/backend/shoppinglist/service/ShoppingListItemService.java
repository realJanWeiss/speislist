package com.speislist.backend.shoppinglist.service;

import com.speislist.backend.shoppinglist.dto.response.ShoppingListItemDTO;
import com.speislist.backend.shoppinglist.entity.ShoppingListItem;
import com.speislist.backend.shoppinglist.exception.ShoppingListItemNotFoundException;
import com.speislist.backend.shoppinglist.repository.ShoppingListItemRepository;
import com.speislist.backend.shoppinglist.util.ShoppingListMapper;
import com.speislist.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingListItemService {
    private final UserService userService;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final ShoppingListService shoppingListService;

    @Transactional
    public ShoppingListItemDTO createShoppingListItem(long shoppingListId, String name, Integer quantity, long userId) {
        final var user = userService.getUserDTOById(userId);
        final var shoppingList = shoppingListService.getShoppingEntityListById(shoppingListId, user.getId());
        final var item = new ShoppingListItem();
        item.setName(name);
        item.setQuantity(quantity);
        item.setIsCompleted(false);
        item.setShoppingList(shoppingList);
        return ShoppingListMapper.toShoppingListItemDTO(shoppingListItemRepository.save(item));
    }

    @Transactional
    public List<ShoppingListItemDTO> getShoppingListItems(long shoppingListId, long userId) {
        final var user = userService.getUserDTOById(userId);
        return shoppingListService.getShoppingListById(shoppingListId, user.getId()).getItems();
    }

    ShoppingListItem getShoppingListItemEntityById(Long id) {
        return shoppingListItemRepository.findById(id)
                .orElseThrow(() -> new ShoppingListItemNotFoundException(id));
    }

    public ShoppingListItemDTO getShoppingListItemById(Long id) {
        return ShoppingListMapper.toShoppingListItemDTO(getShoppingListItemEntityById(id));
    }

    @Transactional
    public ShoppingListItemDTO updateShoppingListItem(Long id, String name, Integer quantity, Boolean isCompleted) {
        final var item = getShoppingListItemEntityById(id);
        if (name != null) item.setName(name);
        if (quantity != null) item.setQuantity(quantity);
        if (isCompleted != null) item.setIsCompleted(isCompleted);
        return ShoppingListMapper.toShoppingListItemDTO(shoppingListItemRepository.save(item));
    }

    @Transactional
    public void deleteShoppingListItem(long shoppingListId, long id, long userId) {
        final var user = userService.getUserDTOById(userId);
        final var shoppingList = shoppingListService.getShoppingListById(shoppingListId, user.getId());
        if (shoppingList.getItems().stream().noneMatch(item -> item.getId().equals(id))) {
            throw new ShoppingListItemNotFoundException(id);
        }
        shoppingListItemRepository.deleteById(id);
    }
}
