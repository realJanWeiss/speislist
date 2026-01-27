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

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShoppingListService {
    private final UserService userService;
    private final ShoppingListRepository shoppingListRepository;
    private final UserShoppingListRepository userShoppingListRepository;

    @Transactional
    public ShoppingListDTO createShoppingList(String name, String userId) {
        final var user = userService.getUserById(userId);
        var shoppingList = new ShoppingList();
        shoppingList.setName(name);

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

    @Transactional
    public ShoppingListDTO getShoppingListById(long id, String userId) {
        final var shoppingList = getShoppingListEntityById(id, userId);
        return ShoppingListMapper.toShoppingListDTO(shoppingList);
    }

    @Transactional
    public ShoppingListDTO updateShoppingList(long id, String name, String userId) {
        final var shoppingList = getShoppingListEntityById(id, userId);
        shoppingList.setName(name);
        final var updatedShoppingList = shoppingListRepository.save(shoppingList);
        return ShoppingListMapper.toShoppingListDTO(updatedShoppingList);
    }

    @Transactional
    public void deleteShoppingList(long id, String userId) {
        final var shoppingList = getShoppingListEntityById(id, userId);
        shoppingListRepository.delete(shoppingList);
    }

    @Transactional
    public ShoppingListDTO addUserToShoppingList(long shoppingListId, @NotNull String userName, String requestingUserId) {
        final var shoppingList = getShoppingListEntityById(shoppingListId, requestingUserId);
        final var user = userService.getUserByUserName(userName);
        final var id = new UserShoppingListId(user.getId(), shoppingListId);
        if (userShoppingListRepository.existsById(id)) {
            return ShoppingListMapper.toShoppingListDTO(shoppingList); // already added
        }
        final var userShoppingList = new UserShoppingList();
        userShoppingList.setId(id);
        userShoppingList.setUser(user);
        userShoppingList.setShoppingList(shoppingList);
        userShoppingListRepository.save(userShoppingList);
        shoppingList.getUserShoppingLists().add(userShoppingList);
        return ShoppingListMapper.toShoppingListDTO(shoppingList);
    }

    @Transactional
    public void removeUserFromShoppingList(long shoppingListId, @NotNull User user, String requestingUserId) {
        final var shoppingList = getShoppingListEntityById(shoppingListId, requestingUserId);
        final var id = new UserShoppingListId(user.getId(), shoppingList.getId());
        userShoppingListRepository.deleteById(id);
    }

    @Transactional
    public void leaveShoppingList(long shoppingListId, String userId) {
        final var shoppingList = getShoppingListEntityById(shoppingListId, userId);
        if (shoppingList.getUserShoppingLists().size() == 1) {
            shoppingListRepository.delete(shoppingList);
        } else {
            final var id = new UserShoppingListId(userId, shoppingList.getId());
            userShoppingListRepository.deleteById(id);
        }
    }

    ShoppingList getShoppingListEntityById(long id) {
        return shoppingListRepository.findById(id).orElseThrow(() -> new ShoppingListNotFoundException(id));
    }

    /**
     * Get ShoppingList and validate user is a member of the shopping list
     */
    ShoppingList getShoppingListEntityById(long id, String userId) {
        final var shoppingList = getShoppingListEntityById(id);
        validateUserCanAccessShoppingList(shoppingList, userId);
        return shoppingList;
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
