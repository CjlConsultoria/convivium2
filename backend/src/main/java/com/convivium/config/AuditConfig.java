package com.convivium.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class AuditConfig implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .filter(auth -> !"anonymousUser".equals(auth.getPrincipal()))
                .map(auth -> {
                    Object principal = auth.getPrincipal();
                    if (principal instanceof Long userId) {
                        return userId;
                    }
                    if (principal instanceof String str) {
                        try {
                            return Long.parseLong(str);
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                    return null;
                });
    }
}
