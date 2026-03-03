package com.convivium.config;

import com.convivium.security.CustomUserDetailsService;
import com.convivium.security.TenantFilter;
import com.convivium.security.jwt.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void securityConfigHasRequiredDependencies() {
        JwtAuthenticationFilter jwtFilter = mock(JwtAuthenticationFilter.class);
        TenantFilter tenantFilter = mock(TenantFilter.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);
        CorsConfigurationSource corsSource = request -> null;

        SecurityConfig config = new SecurityConfig(jwtFilter, tenantFilter, userDetailsService, corsSource);
        assertNotNull(config);
        assertNotNull(SecurityConfig.class.getDeclaredMethods());
    }
}
