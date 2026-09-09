package com.example.user.Dto;

import jakarta.validation.constraints.NotNull;

/** "For recipe R, prefer replacement S wherever it calls for ingredient I." */
public record FavoriteAlternativeRequest(
        @NotNull Long recipeId,
        @NotNull Long ingredientId,
        @NotNull Long replacementIngredientId) {
}
