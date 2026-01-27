package com.speislist.backend.inventory.repository;

import com.speislist.backend.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    @Query("SELECT sl FROM Inventory sl JOIN sl.userInventories usl WHERE usl.user.id = :userId")
    List<Inventory> findByUserId(@Param("userId") String userId);
}
