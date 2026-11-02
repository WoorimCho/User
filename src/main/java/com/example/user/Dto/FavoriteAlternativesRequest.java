package com.example.user.Dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Replace the whole set of favourite substitutions for an account. */
public record FavoriteAlternativesRequest(
        @Valid @Size(max = Limits.FAVORITE_ALTERNATIVES) List<FavoriteAlternativeRequest> alternatives) {

    public List<FavoriteAlternativeRequest> alternativesOrEmpty() {
        return alternatives == null ? List.of() : alternatives;
    }
}
