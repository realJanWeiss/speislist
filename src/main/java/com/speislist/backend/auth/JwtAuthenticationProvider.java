package com.speislist.backend.auth;

import com.speislist.backend.auth.service.JwtService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;

public record JwtAuthenticationProvider(JwtService jwtService) implements AuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        var jwtAuthenticationToken = (JwtAuthenticationToken) authentication;

        final var grantedAuthorityList = new ArrayList<GrantedAuthority>();
        grantedAuthorityList.add(new SimpleGrantedAuthority("ROLE_USER"));

        final var jwt = jwtAuthenticationToken.getJwt();
        final var userId = jwtService.parseToken(jwt);

        if (userId != null) {
            jwtAuthenticationToken = new JwtAuthenticationToken(grantedAuthorityList, userId, jwt);
            SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);
        }

        return jwtAuthenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(JwtAuthenticationToken.class);
    }
}

