package com.speislist.backend.shoppinglist.entity;

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
public class UserShoppingList {
    @EmbeddedId
    private UserShoppingListId id;

    @ManyToOne
    @MapsId("userId")
    private User user;

    @ManyToOne
    @MapsId("shoppingListId")
    private ShoppingList shoppingList;
}
