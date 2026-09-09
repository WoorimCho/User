package com.example.user.Controllers;

import com.example.user.Dto.AuthenticateRequest;
import com.example.user.Dto.AuthenticateResponse;
import com.example.user.Services.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Credential verification. Returns the identity a caller needs; it does not issue
 * a session or token - that belongs to whoever calls this (the BFF), pending the
 * inter-service auth decision.
 */
@RestController
@RequestMapping("/api/authenticate")
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public AuthenticateResponse authenticate(@Valid @RequestBody AuthenticateRequest request) {
        return accountService.authenticate(request);
    }
}
