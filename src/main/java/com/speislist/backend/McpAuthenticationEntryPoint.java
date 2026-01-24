package com.speislist.backend;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class McpAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        // WWW-Authenticate header (strict RFC 6750)
        response.setHeader("WWW-Authenticate",
                "Bearer realm=\"OAuth\", " +
                        "error=\"invalid_token\", " +
                        "error_description=\"Missing or invalid access token\", " +
                        "resource_metadata=\"" + "http://localhost:8080/.well-known/oauth-protected-resource/mcp" + "\""
        );

        // JSON body
        String body = "{\"error\":\"invalid_token\",\"error_description\":\"Missing or invalid access token\"}";
        response.getWriter().write(body);
    }
}
