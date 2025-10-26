package com.speislist.backend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        final var authHeader = request.getHeader("Authorization");

        if (authHeader != null) {
            final var splitHeader = authHeader.split(" ");
            if (splitHeader.length >= 2) {
                final var token = (splitHeader[1]).trim();
                if (!token.isEmpty() && !token.equalsIgnoreCase("undefined")) {
                    authenticationManager.authenticate(new JwtAuthenticationToken(token));
                }
            }
        }


        filterChain.doFilter(request, response);
    }
}

