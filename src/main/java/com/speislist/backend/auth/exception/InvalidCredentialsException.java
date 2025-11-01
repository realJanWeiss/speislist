package com.speislist.backend.auth.exception;

public class InvalidCredentialsException extends IllegalArgumentException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
