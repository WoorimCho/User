package com.example.user.Dto;

import com.example.user.Model.Account;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/** The account, without the password hash. */
public record AccountResponse(
        Long id,
        String username,
        String email,
        String displayName,
        Instant createdAt,
        Instant updatedAt,
        Set<String> restrictions,
        Set<Long> favoriteRecipeIds) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUsername(),
                account.getEmail(),
                account.getDisplayName(),
                account.getCreatedAt(),
                account.getUpdatedAt(),
                Set.copyOf(account.getRestrictions()),
                Set.copyOf(account.getFavoriteRecipeIds()));
    }

    public static List<AccountResponse> from(List<Account> accounts) {
        return accounts.stream().map(AccountResponse::from).toList();
    }
}
