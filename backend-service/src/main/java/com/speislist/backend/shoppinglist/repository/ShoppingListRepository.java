package com.speislist.backend.shoppinglist.repository;

import com.speislist.backend.shoppinglist.entity.ShoppingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, Long> {
    @Query("SELECT sl FROM ShoppingList sl JOIN sl.userShoppingLists usl WHERE usl.user.id = :userId")
    List<ShoppingList> findByUserId(@Param("userId") String userId);
}
