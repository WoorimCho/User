package com.example.user.Dto;

import jakarta.validation.constraints.Size;

import java.util.Set;

/** Replace an account's restriction set. Codes are free text; empty clears them. */
public record RestrictionsRequest(
        @Size(max = Limits.RESTRICTIONS) Set<@Size(max = Limits.RESTRICTION_CODE_LENGTH) String> restrictions) {

    public Set<String> restrictionsOrEmpty() {
        return restrictions == null ? Set.of() : restrictions;
    }
}
