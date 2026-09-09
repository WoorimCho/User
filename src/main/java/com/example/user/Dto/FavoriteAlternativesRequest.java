package com.example.user.Dto;

import jakarta.validation.Valid;

import java.util.List;

/** Replace the whole set of favourite substitutions for an account. */
public record FavoriteAlternativesRequest(@Valid List<FavoriteAlternativeRequest> alternatives) {

    public List<FavoriteAlternativeRequest> alternativesOrEmpty() {
        return alternatives == null ? List.of() : alternatives;
    }
}
