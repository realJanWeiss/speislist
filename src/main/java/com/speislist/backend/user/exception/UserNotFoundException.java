package com.speislist.backend.user.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(UserReferenceType userReferenceType, String userReference) {
        super("User with " + userReferenceType.name().toLowerCase() + " '" + userReference + "' not found.");
    }

    public enum UserReferenceType {
        ID,
        USERNAME
    }
}
