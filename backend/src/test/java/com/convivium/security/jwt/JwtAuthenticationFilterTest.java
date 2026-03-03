package com.convivium.security.jwt;

import com.convivium.module.user.entity.User;
import com.convivium.module.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @Test
    void doFilterInternal_withoutToken_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenProvider, never()).validateToken(any());
    }

    @Test
    void doFilterInternal_withInvalidToken_continuesChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");
        when(jwtTokenProvider.validateToken("invalid")).thenReturn(false);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withValidToken_setsAuthentication() throws Exception {
        SecurityContextHolder.clearContext();
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getSubject(token)).thenReturn(UUID.randomUUID().toString());
        when(jwtTokenProvider.getEmail(token)).thenReturn("a@b.com");
        when(jwtTokenProvider.getCondominiumId(token)).thenReturn(1L);
        when(jwtTokenProvider.getRoles(token)).thenReturn(java.util.List.of("SINDICO"));
        when(jwtTokenProvider.getPermissions(token)).thenReturn(java.util.List.of());
        User user = new User();
        user.setId(1L);
        user.setName("User");
        when(userRepository.findByUuid(any(UUID.class))).thenReturn(Optional.of(user));
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
