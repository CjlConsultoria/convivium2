package com.convivium.module.auth.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.module.auth.dto.LoginRequest;
import com.convivium.module.auth.dto.LoginResponse;
import com.convivium.module.user.entity.User;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.jwt.JwtProperties;
import com.convivium.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCondominiumRoleRepository userCondominiumRoleRepository;

    @Mock
    private com.convivium.module.auth.repository.RefreshTokenRepository refreshTokenRepository;

    @Mock
    private com.convivium.module.auth.repository.PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private com.convivium.module.condominium.repository.CondominiumRepository condominiumRepository;

    @Mock
    private com.convivium.module.condominium.repository.UnitRepository unitRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.convivium.integration.google.GoogleIdTokenVerifier googleIdTokenVerifier;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900_000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604_800_000L);
    }

    @Test
    void login_throwsWhenUserNotFound() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                authService.login(new LoginRequest("a@b.com", "senha")));
    }

    @Test
    void login_throwsWhenPasswordInvalid() {
        User user = User.builder()
                .email("a@b.com")
                .passwordHash("hash")
                .isActive(true)
                .build();
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha", "hash")).thenReturn(false);

        assertThrows(BusinessException.class, () ->
                authService.login(new LoginRequest("a@b.com", "senha")));
    }

    @Test
    void login_returnsTokensWhenValid() {
        User user = User.builder()
                .id(1L)
                .uuid(java.util.UUID.randomUUID())
                .email("a@b.com")
                .passwordHash("hash")
                .name("User")
                .isActive(true)
                .isPlatformAdmin(false)
                .condominiumRoles(List.of())
                .build();
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha", "hash")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any(), any(), any(), any(), any())).thenReturn("accessToken");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refreshToken");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoginResponse response = authService.login(new LoginRequest("a@b.com", "senha"));

        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        assertEquals("refreshToken", response.refreshToken());
        assertNotNull(response.user());
    }
}
