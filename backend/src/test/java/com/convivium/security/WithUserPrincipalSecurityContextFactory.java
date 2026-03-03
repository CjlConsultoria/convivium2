package com.convivium.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WithUserPrincipalSecurityContextFactory implements WithSecurityContextFactory<WithUserPrincipal> {

    @Override
    public SecurityContext createSecurityContext(WithUserPrincipal annotation) {
        List<String> roles = Arrays.asList(annotation.roles());
        UserPrincipal principal = UserPrincipal.builder()
                .id(annotation.id())
                .uuid("test-uuid")
                .email("test@test.com")
                .password("")
                .name("Test User")
                .condominiumId(annotation.condominiumId())
                .isPlatformAdmin(roles.contains("PLATFORM_ADMIN"))
                .active(true)
                .authorities(roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList()))
                .build();

        Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        return ctx;
    }
}
