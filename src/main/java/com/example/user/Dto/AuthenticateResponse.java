package com.example.user.Dto;

import com.example.user.Model.Account;

/** What a caller (e.g. the BFF) needs after a successful credential check to establish its own session. */
public record AuthenticateResponse(Long accountId, String username, String displayName) {

    public static AuthenticateResponse from(Account account) {
        return new AuthenticateResponse(account.getId(), account.getUsername(), account.getDisplayName());
    }
}
