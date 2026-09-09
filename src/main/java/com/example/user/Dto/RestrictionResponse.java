package com.example.user.Dto;

import com.example.user.Model.Restriction;

public record RestrictionResponse(Long id, String code, String label, String kind, String description) {

    public static RestrictionResponse from(Restriction restriction) {
        return new RestrictionResponse(restriction.getId(), restriction.getCode(),
                restriction.getLabel(), restriction.getKind(), restriction.getDescription());
    }
}
