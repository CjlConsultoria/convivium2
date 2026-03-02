package com.convivium.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class CorsConfig {

    private static final String ENV_APP_FRONTEND_URL = "APP_FRONTEND_URL";
    private static final String ENV_CORS_ALLOWED_ORIGINS = "CORS_ALLOWED_ORIGINS";

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    @Bean
    @ConfigurationProperties(prefix = "app.cors")
    public CorsProperties corsProperties() {
        return new CorsProperties();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        // 1) Origens do YAML (app.cors.allowed-origins)
        List<String> origins = corsProperties.getAllowedOrigins().stream()
                .flatMap(s -> s.contains(",") ? Stream.of(s.split("\\s*,\\s*")) : Stream.of(s))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty() && !origin.startsWith("${"))
                .collect(Collectors.toList());

        // 2) Fallback: variáveis de ambiente (garante funcionar no Render mesmo se o YAML não resolver)
        Set<String> combined = new LinkedHashSet<>(origins);
        String fromEnv = System.getenv(ENV_CORS_ALLOWED_ORIGINS);
        if (fromEnv != null && !fromEnv.isBlank()) {
            Arrays.stream(fromEnv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).forEach(combined::add);
        }
        String frontendUrl = System.getenv(ENV_APP_FRONTEND_URL);
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            combined.add(frontendUrl.trim());
        }
        origins = new ArrayList<>(combined);

        if (origins.isEmpty()) {
            origins = corsProperties.getAllowedOrigins();
        }
        log.info("CORS allowed origins: {}", origins);

        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    public static class CorsProperties {
        private List<String> allowedOrigins = List.of("*");
        private List<String> allowedMethods = List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
        private List<String> allowedHeaders = List.of("*");
        private List<String> exposedHeaders = List.of("Authorization");
        private boolean allowCredentials = true;
        private long maxAge = 3600L;

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }

        public List<String> getAllowedMethods() {
            return allowedMethods;
        }

        public void setAllowedMethods(List<String> allowedMethods) {
            this.allowedMethods = allowedMethods;
        }

        public List<String> getAllowedHeaders() {
            return allowedHeaders;
        }

        public void setAllowedHeaders(List<String> allowedHeaders) {
            this.allowedHeaders = allowedHeaders;
        }

        public List<String> getExposedHeaders() {
            return exposedHeaders;
        }

        public void setExposedHeaders(List<String> exposedHeaders) {
            this.exposedHeaders = exposedHeaders;
        }

        public boolean isAllowCredentials() {
            return allowCredentials;
        }

        public void setAllowCredentials(boolean allowCredentials) {
            this.allowCredentials = allowCredentials;
        }

        public long getMaxAge() {
            return maxAge;
        }

        public void setMaxAge(long maxAge) {
            this.maxAge = maxAge;
        }
    }
}
