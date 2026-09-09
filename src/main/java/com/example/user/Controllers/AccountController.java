package com.example.user.Controllers;

import com.example.user.Dto.AccountResponse;
import com.example.user.Dto.FavoriteAlternativeResponse;
import com.example.user.Dto.FavoriteAlternativesRequest;
import com.example.user.Dto.PasswordChangeRequest;
import com.example.user.Dto.ProfileUpdateRequest;
import com.example.user.Dto.RegisterRequest;
import com.example.user.Dto.RestrictionsRequest;
import com.example.user.Services.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse register(@Valid @RequestBody RegisterRequest request) {
        return accountService.register(request);
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable long id) {
        return accountService.get(id);
    }

    @PutMapping("/{id}")
    public AccountResponse updateProfile(@PathVariable long id, @Valid @RequestBody ProfileUpdateRequest request) {
        return accountService.updateProfile(id, request);
    }

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable long id, @Valid @RequestBody PasswordChangeRequest request) {
        accountService.changePassword(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        accountService.delete(id);
    }

    // --- Dietary restrictions ---------------------------------------------------

    @GetMapping("/{id}/restrictions")
    public Set<String> getRestrictions(@PathVariable long id) {
        return accountService.getRestrictions(id);
    }

    @PutMapping("/{id}/restrictions")
    public AccountResponse replaceRestrictions(@PathVariable long id, @RequestBody RestrictionsRequest request) {
        return accountService.replaceRestrictions(id, request.restrictionsOrEmpty());
    }

    // --- Favourite recipes (a variation = its own recipe id) ------------------

    @GetMapping("/{id}/favorites/recipes")
    public Set<Long> getFavoriteRecipes(@PathVariable long id) {
        return accountService.getFavoriteRecipeIds(id);
    }

    @PostMapping("/{id}/favorites/recipes/{recipeId}")
    public AccountResponse addFavoriteRecipe(@PathVariable long id, @PathVariable long recipeId) {
        return accountService.addFavoriteRecipe(id, recipeId);
    }

    @DeleteMapping("/{id}/favorites/recipes/{recipeId}")
    public AccountResponse removeFavoriteRecipe(@PathVariable long id, @PathVariable long recipeId) {
        return accountService.removeFavoriteRecipe(id, recipeId);
    }

    // --- Favourite substitutions --------------------------------------------------

    @GetMapping("/{id}/favorites/alternatives")
    public List<FavoriteAlternativeResponse> getFavoriteAlternatives(@PathVariable long id) {
        return accountService.getFavoriteAlternatives(id);
    }

    @PutMapping("/{id}/favorites/alternatives")
    public List<FavoriteAlternativeResponse> replaceFavoriteAlternatives(
            @PathVariable long id, @Valid @RequestBody FavoriteAlternativesRequest request) {
        return accountService.replaceFavoriteAlternatives(id, request.alternativesOrEmpty());
    }

    @DeleteMapping("/{id}/favorites/alternatives/{alternativeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFavoriteAlternative(@PathVariable long id, @PathVariable long alternativeId) {
        accountService.removeFavoriteAlternative(id, alternativeId);
    }
}
