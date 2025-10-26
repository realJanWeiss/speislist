package com.speislist.backend.auth;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@EqualsAndHashCode(callSuper = true)
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Long userId;
    @Getter
    private final String jwt;

    public JwtAuthenticationToken(Collection<? extends GrantedAuthority> authorities,
                                  Long userId,
                                  String jwt) {
        super(authorities);
        this.userId = userId;
        this.jwt = jwt;
        setAuthenticated(true);
    }

    public JwtAuthenticationToken(String jwt) {
        this(null, null, jwt);
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }
}
