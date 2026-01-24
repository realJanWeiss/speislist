package com.speislist.backend.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class JwtUserService {
    public JwtUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException("No authenticated user found");
        }

        return new JwtUser(
            jwt.getSubject(),
            jwt.getClaim("email"),
            jwt.getClaim("given_name"),
            jwt.getClaim("family_name")
        );
    }
}
