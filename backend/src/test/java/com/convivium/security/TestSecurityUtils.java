package com.convivium.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utilitários para configurar autenticação em testes MockMvc.
 * Usa RequestPostProcessor para garantir que o UserPrincipal seja propagado corretamente.
 */
public final class TestSecurityUtils {

    private TestSecurityUtils() {
    }

    /**
     * Cria um RequestPostProcessor com UserPrincipal padrão (id=1, condominiumId=1, SINDICO).
     */
    public static RequestPostProcessor withUserPrincipal() {
        return withUserPrincipal(1L, 1L, "SINDICO");
    }

    /**
     * Cria um RequestPostProcessor com UserPrincipal customizado.
     */
    public static RequestPostProcessor withUserPrincipal(long id, long condominiumId, String... roles) {
        List<String> roleList = Arrays.asList(roles);
        UserPrincipal principal = UserPrincipal.builder()
                .id(id)
                .uuid("test-uuid")
                .email("test@test.com")
                .password("")
                .name("Test User")
                .condominiumId(condominiumId)
                .isPlatformAdmin(roleList.contains("PLATFORM_ADMIN"))
                .active(true)
                .authorities(roleList.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList()))
                .build();

        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    /**
     * Define o SecurityContext no thread atual (para uso com @WithUserPrincipal quando necessário).
     */
    public static void setSecurityContext(UserPrincipal principal) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(ctx);
    }
}
