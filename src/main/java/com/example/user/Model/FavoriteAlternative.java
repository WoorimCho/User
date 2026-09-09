package com.example.user.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

/**
 * "For recipe R, when it calls for ingredient I, I prefer replacement S." Lets
 * the BFF auto-apply a substitution when rendering recipe R for this account.
 * One preference per (account, recipe, ingredient).
 */
@Entity
@Table(name = "favorite_alternative", uniqueConstraints = @UniqueConstraint(
        name = "uk_favorite_alternative",
        columnNames = {"account_id", "recipe_id", "ingredient_id"}))
@Getter
public class FavoriteAlternative {

    @Id
    @GeneratedValue
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Setter
    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Setter
    @Column(name = "ingredient_id", nullable = false)
    private Long ingredientId;

    @Setter
    @Column(name = "replacement_ingredient_id", nullable = false)
    private Long replacementIngredientId;

    protected FavoriteAlternative() {
        // for JPA
    }

    public FavoriteAlternative(Long recipeId, Long ingredientId, Long replacementIngredientId) {
        this.recipeId = recipeId;
        this.ingredientId = ingredientId;
        this.replacementIngredientId = replacementIngredientId;
    }
}
