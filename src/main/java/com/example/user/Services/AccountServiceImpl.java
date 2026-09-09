package com.example.user.Services;

import com.example.user.Dto.AccountResponse;
import com.example.user.Dto.AuthenticateRequest;
import com.example.user.Dto.AuthenticateResponse;
import com.example.user.Dto.FavoriteAlternativeRequest;
import com.example.user.Dto.FavoriteAlternativeResponse;
import com.example.user.Dto.PasswordChangeRequest;
import com.example.user.Dto.ProfileUpdateRequest;
import com.example.user.Dto.RegisterRequest;
import com.example.user.Exception.InvalidCredentialsException;
import com.example.user.Exception.NotFoundException;
import com.example.user.Model.Account;
import com.example.user.Model.FavoriteAlternative;
import com.example.user.Repositories.AccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accounts;
    private final PasswordEncoder passwordEncoder;

    public AccountServiceImpl(AccountRepository accounts, PasswordEncoder passwordEncoder) {
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AccountResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = normaliseEmail(request.email());
        if (accounts.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("username already taken");
        }
        if (email != null && accounts.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("email already registered");
        }
        Account account = new Account(username, email, request.displayName().trim(),
                passwordEncoder.encode(request.password()));
        return AccountResponse.from(accounts.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse get(long id) {
        return AccountResponse.from(require(id));
    }

    @Override
    public AccountResponse updateProfile(long id, ProfileUpdateRequest request) {
        Account account = require(id);
        String email = normaliseEmail(request.email());
        if (email != null && !email.equalsIgnoreCase(account.getEmail())
                && accounts.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("email already registered");
        }
        account.setEmail(email);
        account.setDisplayName(request.displayName().trim());
        return AccountResponse.from(accounts.save(account));
    }

    @Override
    public void changePassword(long id, PasswordChangeRequest request) {
        Account account = require(id);
        if (!passwordEncoder.matches(request.currentPassword(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        account.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        accounts.save(account);
    }

    @Override
    public void delete(long id) {
        accounts.delete(require(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        String identifier = request.identifier().trim();
        Account account = accounts.findByUsernameIgnoreCase(identifier)
                .or(() -> accounts.findByEmailIgnoreCase(identifier))
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return AuthenticateResponse.from(account);
    }

    @Override
    public AccountResponse replaceRestrictions(long id, Set<String> codes) {
        Account account = require(id);
        Set<String> normalised = codes.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(code -> code.trim().toLowerCase(java.util.Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        account.replaceRestrictions(normalised);
        return AccountResponse.from(accounts.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> getRestrictions(long id) {
        return Set.copyOf(require(id).getRestrictions());
    }

    @Override
    public AccountResponse addFavoriteRecipe(long id, long recipeId) {
        Account account = require(id);
        account.getFavoriteRecipeIds().add(recipeId);
        return AccountResponse.from(accounts.save(account));
    }

    @Override
    public AccountResponse removeFavoriteRecipe(long id, long recipeId) {
        Account account = require(id);
        account.getFavoriteRecipeIds().remove(recipeId);
        return AccountResponse.from(accounts.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getFavoriteRecipeIds(long id) {
        return Set.copyOf(require(id).getFavoriteRecipeIds());
    }

    @Override
    public List<FavoriteAlternativeResponse> replaceFavoriteAlternatives(
            long id, List<FavoriteAlternativeRequest> alternatives) {
        Account account = require(id);
        List<FavoriteAlternative> entities = alternatives.stream()
                .map(a -> new FavoriteAlternative(a.recipeId(), a.ingredientId(), a.replacementIngredientId()))
                .toList();
        account.replaceFavoriteAlternatives(entities);
        Account saved = accounts.save(account);
        return saved.getFavoriteAlternatives().stream().map(FavoriteAlternativeResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteAlternativeResponse> getFavoriteAlternatives(long id) {
        return require(id).getFavoriteAlternatives().stream()
                .map(FavoriteAlternativeResponse::from)
                .toList();
    }

    @Override
    public void removeFavoriteAlternative(long id, long alternativeId) {
        Account account = require(id);
        boolean removed = account.getFavoriteAlternatives()
                .removeIf(alt -> alt.getId().equals(alternativeId));
        if (!removed) {
            throw NotFoundException.of("Favorite alternative", alternativeId);
        }
        accounts.save(account);
    }

    private Account require(long id) {
        return accounts.findById(id).orElseThrow(() -> NotFoundException.of("Account", id));
    }

    /** Blank email -> null: it's optional, and the unique index treats every NULL as distinct. */
    private static String normaliseEmail(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
