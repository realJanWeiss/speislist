package com.speislist.backend.shoppinglist.service;

import com.speislist.backend.shoppinglist.dto.response.ShoppingListDTO;
import com.speislist.backend.shoppinglist.entity.ShoppingList;
import com.speislist.backend.shoppinglist.entity.UserShoppingList;
import com.speislist.backend.shoppinglist.entity.UserShoppingListId;
import com.speislist.backend.shoppinglist.exception.ShoppingListNotFoundException;
import com.speislist.backend.shoppinglist.repository.ShoppingListRepository;
import com.speislist.backend.shoppinglist.repository.UserShoppingListRepository;
import com.speislist.backend.shoppinglist.util.ShoppingListMapper;
import com.speislist.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShoppingListService {
    private final ShoppingListRepository shoppingListRepository;
    private final UserShoppingListRepository userShoppingListRepository;

    @Transactional
    public ShoppingListDTO createShoppingList(String name, User creator) {
        var shoppingList = new ShoppingList();
        shoppingList.setName(name);
        shoppingList.setCreatedAt(LocalDateTime.now());
        shoppingList = shoppingListRepository.save(shoppingList);

        var userShoppingList = new UserShoppingList();
        userShoppingList.setId(new UserShoppingListId(creator.getId(), shoppingList.getId()));
        userShoppingList.setUser(creator);
        userShoppingList.setShoppingList(shoppingList);
        userShoppingListRepository.save(userShoppingList);

        return ShoppingListMapper.toShoppingListDTO(shoppingList);
    }

    public List<ShoppingListDTO> getShoppingListsByUser(User user) {
        return shoppingListRepository.findByUserId(user.getId()).stream()
                .map(ShoppingListMapper::toShoppingListDTO)
                .toList();
    }

    ShoppingList getShoppingEntityListById(Long id) {
        return shoppingListRepository.findById(id)
                .orElseThrow(() -> new ShoppingListNotFoundException(id));
    }

    public ShoppingListDTO getShoppingListById(Long id) {
        final var shoppingList = getShoppingEntityListById(id);
        return ShoppingListMapper.toShoppingListDTO(shoppingList);
    }

    @Transactional
    public ShoppingListDTO updateShoppingList(Long id, String name) {
        final var shoppingList = getShoppingEntityListById(id);
        shoppingList.setName(name);
        return ShoppingListMapper.toShoppingListDTO(shoppingListRepository.save(shoppingList));
    }

    @Transactional
    public void deleteShoppingList(Long id) {
        if (!shoppingListRepository.existsById(id)) {
            throw new ShoppingListNotFoundException(id);
        }
        shoppingListRepository.deleteById(id);
    }

    @Transactional
    public void addUserToShoppingList(Long shoppingListId, User user) {
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
    public void removeUserFromShoppingList(Long shoppingListId, User user) {
        UserShoppingListId id = new UserShoppingListId(user.getId(), shoppingListId);
        userShoppingListRepository.deleteById(id);
    }
}
