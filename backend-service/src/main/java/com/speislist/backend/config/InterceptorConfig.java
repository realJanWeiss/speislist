package com.speislist.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final IdentityProviderUpsertInterceptor identityProviderUpsertInterceptor;

    @Autowired
    public InterceptorConfig(IdentityProviderUpsertInterceptor identityProviderUpsertInterceptor) {
        this.identityProviderUpsertInterceptor = identityProviderUpsertInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(identityProviderUpsertInterceptor).addPathPatterns("/**");
    }
}
