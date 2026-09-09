package com.example.user.Dto;

import com.example.user.Model.FavoriteAlternative;

public record FavoriteAlternativeResponse(
        Long id,
        Long recipeId,
        Long ingredientId,
        Long replacementIngredientId) {

    public static FavoriteAlternativeResponse from(FavoriteAlternative alternative) {
        return new FavoriteAlternativeResponse(alternative.getId(), alternative.getRecipeId(),
                alternative.getIngredientId(), alternative.getReplacementIngredientId());
    }
}
