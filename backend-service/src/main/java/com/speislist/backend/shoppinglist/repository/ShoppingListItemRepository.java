package com.speislist.backend.shoppinglist.repository;

import com.speislist.backend.shoppinglist.entity.ShoppingListItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ShoppingListItemRepository extends JpaRepository<ShoppingListItem, Long> {

    List<ShoppingListItem> findByIdIn(Collection<Long> ids);

    void deleteByIdIn(Collection<Long> ids);
}
