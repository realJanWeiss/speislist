package com.speislist.backend.shoppinglist.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserShoppingListId implements Serializable {
    private Long userId;
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
