package com.example.user.Dto;

import jakarta.validation.constraints.NotBlank;

/** Verify credentials. {@code identifier} is a username or an email. */
public record AuthenticateRequest(
        @NotBlank String identifier,
        @NotBlank String password) {
}
