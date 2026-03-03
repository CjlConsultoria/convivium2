package com.convivium.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CorsConfigTest {

    @Test
    void corsConfigurationSource_includesAllowedOriginsAndMethods() {
        CorsConfig.CorsProperties props = new CorsConfig.CorsProperties();
        props.setAllowedOrigins(List.of("http://localhost:5173", "https://convivium2.onrender.com"));
        props.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        props.setAllowedHeaders(List.of("*"));
        props.setAllowCredentials(true);

        CorsConfig config = new CorsConfig();
        CorsConfigurationSource source = config.corsConfigurationSource(props);

        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        CorsConfiguration corsConfig = source.getCorsConfiguration(request);

        assertNotNull(corsConfig);
        assertTrue(corsConfig.getAllowedOrigins().contains("http://localhost:5173"));
        assertTrue(corsConfig.getAllowedOrigins().contains("https://convivium2.onrender.com"));
        assertTrue(corsConfig.getAllowedMethods().contains("OPTIONS"));
        assertTrue(corsConfig.getAllowedMethods().contains("POST"));
        assertTrue(corsConfig.getAllowCredentials());
        assertEquals("*", corsConfig.getAllowedHeaders().get(0));
    }
}
