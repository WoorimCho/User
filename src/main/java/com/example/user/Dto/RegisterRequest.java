package com.example.user.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * New account. {@code email} is optional (nothing is sent to it) — omit it or
 * pass blank; when given it must look like an email and stays unique.
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String displayName,
        @NotBlank @Size(min = 8, max = 100) String password) {
}
