package com.example.user.Controllers;

import com.example.user.Dto.RestrictionResponse;
import com.example.user.Services.RestrictionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The advisory catalogue of common dietary restrictions (seeded). Accounts store
 * restriction codes as free text; this endpoint is for populating pickers and
 * showing labels.
 */
@RestController
@RequestMapping("/api/restrictions")
public class RestrictionController {

    private final RestrictionService restrictionService;

    public RestrictionController(RestrictionService restrictionService) {
        this.restrictionService = restrictionService;
    }

    /** {@code GET /api/restrictions} or {@code ?kind=diet|allergen|religious|lifestyle}. */
    @GetMapping
    public List<RestrictionResponse> list(@RequestParam(required = false) String kind) {
        return restrictionService.list(kind);
    }

    @GetMapping("/{code}")
    public RestrictionResponse get(@PathVariable String code) {
        return restrictionService.getByCode(code);
    }
}
