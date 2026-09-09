package com.example.user.Dto;

import java.util.Set;

/** Replace an account's restriction set. Codes are free text; empty clears them. */
public record RestrictionsRequest(Set<String> restrictions) {

    public Set<String> restrictionsOrEmpty() {
        return restrictions == null ? Set.of() : restrictions;
    }
}
