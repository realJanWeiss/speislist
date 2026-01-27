package com.speislist.backend.inventory.entity;

import com.speislist.backend.user.entity.User;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserInventory {
    @EmbeddedId
    private UserInventoryId id;

    @ManyToOne
    @MapsId("userId")
    private User user;

    @ManyToOne
    @MapsId("inventoryId")
    private Inventory inventory;
}
