package com.speislist.backend.user.exception;

public class UserAlreadyExistsException extends IllegalArgumentException {
    public UserAlreadyExistsException(String email) {
        super("User with email " + email + " already exists.");
    }
}