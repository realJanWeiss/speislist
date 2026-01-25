package com.speislist.backend.shoppinglist.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserShoppingListId implements Serializable {
    private String userId;
    private Long shoppingListId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserShoppingListId that = (UserShoppingListId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(shoppingListId, that.shoppingListId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, shoppingListId);
    }
}
