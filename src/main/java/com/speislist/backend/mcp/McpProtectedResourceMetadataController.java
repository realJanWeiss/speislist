package com.speislist.backend.mcp;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(McpProtectedResourceMetadataController.WELL_KNOWN_BASE)
public class McpProtectedResourceMetadataController {

    public static final String MCP_RESOURCE_PATH = "/mcp";
    public static final String WELL_KNOWN_BASE = "/.well-known/oauth-protected-resource";
    public static final String METADATA_ENDPOINT = WELL_KNOWN_BASE + MCP_RESOURCE_PATH;

    private final String issuerUri;

    public McpProtectedResourceMetadataController(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        this.issuerUri = issuerUri;
    }

    @GetMapping(MCP_RESOURCE_PATH)
    public Map<String, Object> metadata(@NonNull HttpServletRequest request) {
        return Map.of(
                "resource", resourceUri(request),
                "authorization_servers", List.of(issuerUri),
                "scopes_supported", List.of("openid", "profile", "email"));
    }

    private String resourceUri(@NonNull HttpServletRequest request) {
        return baseUrl(request) + MCP_RESOURCE_PATH;
    }

    private String baseUrl(@NonNull HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromContextPath(request)
                .replaceQuery(null)
                .build()
                .toUriString();
    }
}
