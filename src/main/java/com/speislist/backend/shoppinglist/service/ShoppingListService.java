package com.speislist.backend.shoppinglist.service;

import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
import com.speislist.backend.shoppinglist.entity.ShoppingList;
import com.speislist.backend.shoppinglist.entity.UserShoppingList;
import com.speislist.backend.shoppinglist.entity.UserShoppingListId;
import com.speislist.backend.shoppinglist.exception.ShoppingListNotFoundException;
import com.speislist.backend.shoppinglist.repository.ShoppingListRepository;
import com.speislist.backend.shoppinglist.repository.UserShoppingListRepository;
import com.speislist.backend.shoppinglist.util.ShoppingListMapper;
import com.speislist.backend.user.UserService;
import com.speislist.backend.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShoppingListService {
    private final UserService userService;
    private final ShoppingListRepository shoppingListRepository;
    private final UserShoppingListRepository userShoppingListRepository;

    @Transactional
    public ShoppingListDTO createShoppingList(String name, String userId, String email) {
        final var user = userService.upsertFromIdentityProvider(userId, email);

        var shoppingList = new ShoppingList();
        shoppingList.setName(name);
        shoppingList.setCreatedAt(LocalDateTime.now());

        final var userShoppingList = new UserShoppingList();
        userShoppingList.setId(new UserShoppingListId(user.getId(), shoppingList.getId()));
        userShoppingList.setUser(user);
        userShoppingList.setShoppingList(shoppingList);

        shoppingList.setUserShoppingLists(Set.of(userShoppingList));

        shoppingList = shoppingListRepository.save(shoppingList);
        return ShoppingListMapper.toShoppingListDTO(shoppingList);
    }

    public List<ShoppingListDTO> getShoppingListsByUser(String userId) {
        return shoppingListRepository.findByUserId(userId).stream()
                .map(ShoppingListMapper::toShoppingListDTO)
                .toList();
    }

    ShoppingList getShoppingEntityListById(long id) {
        return shoppingListRepository.findById(id)
                .orElseThrow(() -> new ShoppingListNotFoundException(id));
    }

    /**
     * Get ShoppingList and validate user is a member of the shopping list
     */
    ShoppingList getShoppingEntityListById(long id, String userId) {
        final var shoppingList = getShoppingEntityListById(id);
        if (!isMemberOfShoppingList(shoppingList, userId)) {
            throw new ShoppingListNotFoundException(id);
        }
        return shoppingList;
    }

    @Transactional
    public ShoppingListDTO getShoppingListById(long id, String userId) {
        final var shoppingList = getShoppingEntityListById(id, userId);
        return ShoppingListMapper.toShoppingListDTO(shoppingList);
    }

    @Transactional
    public ShoppingListDTO updateShoppingList(long id, String name, String userId) {
        final var shoppingList = getShoppingEntityListById(id, userId);
        shoppingList.setName(name);
        return ShoppingListMapper.toShoppingListDTO(shoppingListRepository.save(shoppingList));
    }

    @Transactional
    public void deleteShoppingList(long id, String userId) {
        final var shoppingList = getShoppingEntityListById(id);
        if (!isMemberOfShoppingList(shoppingList, userId)) {
            throw new ShoppingListNotFoundException(id);
        }
        shoppingListRepository.deleteById(id);
    }

    @Transactional
    public void addUserToShoppingList(long shoppingListId, @NotNull User user) {
        final var shoppingList = getShoppingEntityListById(shoppingListId);
        UserShoppingListId id = new UserShoppingListId(user.getId(), shoppingListId);
        if (userShoppingListRepository.existsById(id)) {
            return; // already added
        }
        UserShoppingList userShoppingList = new UserShoppingList();
        userShoppingList.setId(id);
        userShoppingList.setUser(user);
        userShoppingList.setShoppingList(shoppingList);
        userShoppingListRepository.save(userShoppingList);
    }

    @Transactional
    public void removeUserFromShoppingList(long shoppingListId, @NotNull User user) {
        UserShoppingListId id = new UserShoppingListId(user.getId(), shoppingListId);
        userShoppingListRepository.deleteById(id);
    }

    private boolean isMemberOfShoppingList(@NotNull ShoppingList shoppingList, String userId) {
        return shoppingList.getUserShoppingLists().stream()
                .anyMatch(userShoppingList -> userShoppingList.getUser().getId().equals(userId));
    }

    void validateUserCanAccessShoppingList(@NotNull ShoppingList shoppingList, String userId) {
        if (!isMemberOfShoppingList(shoppingList, userId)) {
            throw new ShoppingListNotFoundException(shoppingList.getId());
        }
    }
}
