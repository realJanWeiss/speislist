package com.speislist.backend.shoppinglist.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
public class ShoppingList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "shoppingList", cascade = CascadeType.PERSIST)
    private Set<UserShoppingList> userShoppingLists;

    @OneToMany(mappedBy = "shoppingList")
    private List<ShoppingListItem> items;
}
