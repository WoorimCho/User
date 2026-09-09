package com.example.user.Services;

import com.example.user.Dto.RestrictionResponse;

import java.util.List;

public interface RestrictionService {

    /** The advisory catalogue of common restrictions, optionally filtered by kind. */
    List<RestrictionResponse> list(String kind);

    RestrictionResponse getByCode(String code);
}
