package com.speislist.backend.config;

import com.speislist.backend.security.JwtUserService;
import com.speislist.backend.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class IdentityProviderUpsertInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(IdentityProviderUpsertInterceptor.class);

    private final JwtUserService jwtUserService;
    private final UserService userService;

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        try {
            final var jwtUser = jwtUserService.getCurrentUser();
            if (jwtUser != null) {
                final var userId = jwtUser.userId();
                final var userName = jwtUser.userName();
                if (userId != null && userName != null) {
                    userService.upsertFromIdentityProvider(userId, userName);
                } else {
                    log.debug(
                            "Skipping upsert: missing userId or userName from identity provider (userId={}"
                                    + " userName={})",
                            userId,
                            userName);
                }
            }
        } catch (Exception ex) {
            log.warn("Failed to upsert user from identity provider", ex);
        }

        return true;
    }
}
