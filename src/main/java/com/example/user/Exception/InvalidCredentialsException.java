package com.example.user.Exception;

/** Wrong identifier or password on authenticate; mapped to HTTP 401. */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
