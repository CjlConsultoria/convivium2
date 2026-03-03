package com.convivium.security;

import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.repository.CondominiumRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantFilterTest {

    @Mock
    private CondominiumRepository condominiumRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TenantFilter tenantFilter;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
    }

    @Test
    void doFilterInternal_withoutAuthentication_continuesChain() throws Exception {
        SecurityContextHolder.clearContext();

        tenantFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withUserAndCondoPath_setsTenantContext() throws Exception {
        UserPrincipal principal = UserPrincipal.builder()
                .id(1L)
                .uuid("uuid")
                .email("a@b.com")
                .password("")
                .name("User")
                .condominiumId(1L)
                .isPlatformAdmin(false)
                .active(true)
                .authorities(java.util.List.of())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        when(request.getRequestURI()).thenReturn("/api/v1/condos/1/users");
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(Condominium.builder().id(1L).name("Condo").slug("condo").status("ACTIVE").build()));

        tenantFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
