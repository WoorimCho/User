package com.example.user.Services;

import com.example.user.Dto.AccountResponse;
import com.example.user.Dto.AuthenticateRequest;
import com.example.user.Dto.AuthenticateResponse;
import com.example.user.Dto.FavoriteAlternativeRequest;
import com.example.user.Dto.FavoriteAlternativeResponse;
import com.example.user.Dto.PasswordChangeRequest;
import com.example.user.Dto.ProfileUpdateRequest;
import com.example.user.Dto.RegisterRequest;

import java.util.List;
import java.util.Set;

public interface AccountService {

    AccountResponse register(RegisterRequest request);

    AccountResponse get(long id);

    AccountResponse updateProfile(long id, ProfileUpdateRequest request);

    void changePassword(long id, PasswordChangeRequest request);

    void delete(long id);

    /** Verify an identifier + password. Throws {@code InvalidCredentialsException} on any mismatch. */
    AuthenticateResponse authenticate(AuthenticateRequest request);

    AccountResponse replaceRestrictions(long id, Set<String> codes);

    Set<String> getRestrictions(long id);

    AccountResponse addFavoriteRecipe(long id, long recipeId);

    AccountResponse removeFavoriteRecipe(long id, long recipeId);

    Set<Long> getFavoriteRecipeIds(long id);

    List<FavoriteAlternativeResponse> replaceFavoriteAlternatives(long id, List<FavoriteAlternativeRequest> alternatives);

    List<FavoriteAlternativeResponse> getFavoriteAlternatives(long id);

    void removeFavoriteAlternative(long id, long alternativeId);
}
