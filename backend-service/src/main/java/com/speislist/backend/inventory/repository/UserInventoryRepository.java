package com.speislist.backend.inventory.repository;

import com.speislist.backend.inventory.entity.UserInventory;
import com.speislist.backend.inventory.entity.UserInventoryId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInventoryRepository extends JpaRepository<UserInventory, UserInventoryId> {
}
