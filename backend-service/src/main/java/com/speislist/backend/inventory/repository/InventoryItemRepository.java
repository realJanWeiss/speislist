package com.speislist.backend.inventory.repository;

import com.speislist.backend.inventory.entity.InventoryItem;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByIdIn(Collection<Long> ids);

    void deleteByIdIn(Collection<Long> ids);
}
