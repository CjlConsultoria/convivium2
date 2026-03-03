package com.convivium.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Configura o MockMvc com springSecurity() para que RequestPostProcessors
 * (ex: SecurityMockMvcRequestPostProcessors.authentication) funcionem corretamente
 * na resolução de @CurrentUser / @AuthenticationPrincipal.
 * Também importa WebMvcConfig para garantir que CurrentUserArgumentResolver seja carregado.
 */
@TestConfiguration
@Import({WebMvcConfig.class, TestSecurityConfig.class})
public class MockMvcSecurityConfig {

    @Bean
    public MockMvc mockMvc(WebApplicationContext webApplicationContext) {
        return MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }
}
