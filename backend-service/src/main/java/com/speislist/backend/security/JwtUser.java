package com.speislist.backend.security;

public record JwtUser(
        String userId,
        String userName,
        String email,
        String firstName,
        String lastName
) {
}
