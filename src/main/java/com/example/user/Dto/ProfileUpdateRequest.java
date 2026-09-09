package com.example.user.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Editable profile fields. Username is immutable; password changes go through
 * their own endpoint. {@code email} is optional — pass blank to clear it.
 */
public record ProfileUpdateRequest(
        @Email @Size(max = 255) String email,
        @NotBlank @Size(max = 100) String displayName) {
}
