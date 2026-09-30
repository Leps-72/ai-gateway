package com.aigateway.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SecurityConfigTest {

    @Test
    void configuresExplicitFrontendOriginsWithoutWildcard() {
        SecurityConfig securityConfig = new SecurityConfig();
        CorsConfigurationSource source = securityConfig.corsConfigurationSource(
                "http://localhost:5173, https://console.example.com"
        );
        HttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/usage");

        CorsConfiguration configuration = source.getCorsConfiguration(request);

        assertNotNull(configuration);
        assertEquals(
                java.util.List.of("http://localhost:5173", "https://console.example.com"),
                configuration.getAllowedOrigins()
        );
        assertFalse(configuration.getAllowedOrigins().contains("*"));
        assertEquals(java.util.List.of("GET", "POST", "OPTIONS"), configuration.getAllowedMethods());
        assertEquals(
                java.util.List.of("Authorization", "Content-Type"),
                configuration.getAllowedHeaders()
        );
    }
}
