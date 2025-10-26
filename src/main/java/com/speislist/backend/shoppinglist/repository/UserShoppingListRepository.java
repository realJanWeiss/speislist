package com.speislist.backend.shoppinglist.repository;

import com.speislist.backend.shoppinglist.entity.UserShoppingList;
import com.speislist.backend.shoppinglist.entity.UserShoppingListId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserShoppingListRepository extends JpaRepository<UserShoppingList, UserShoppingListId> {
}
