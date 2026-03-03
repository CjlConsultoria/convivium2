package com.convivium.module.auth.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.common.exception.ResourceNotFoundException;
import com.convivium.module.auth.dto.*;
import com.convivium.module.auth.entity.PasswordResetToken;
import com.convivium.module.auth.entity.RefreshToken;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.entity.Unit;
import com.convivium.module.user.entity.Role;
import com.convivium.module.user.entity.User;
import com.convivium.module.user.entity.UserCondominiumRole;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.jwt.JwtProperties;
import com.convivium.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900_000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604_800_000L);
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

    @Test
    void login_throwsWhenPasswordHashNull() {
        User user = User.builder().email("a@b.com").passwordHash(null).isActive(true).build();
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));

        BusinessException ex = assertThrows(BusinessException.class, () ->
                authService.login(new LoginRequest("a@b.com", "senha")));
        assertEquals("USE_GOOGLE_LOGIN", ex.getErrorCode());
    }

    @Test
    void login_throwsWhenUserInactive() {
        User user = User.builder().email("a@b.com").passwordHash("hash").isActive(false).build();
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha", "hash")).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class, () ->
                authService.login(new LoginRequest("a@b.com", "senha")));
        assertEquals("ACCOUNT_DISABLED", ex.getErrorCode());
    }

    @Test
    void refreshToken_returnsNewTokens() {
        when(jwtProperties.getAccessTokenExpiration()).thenReturn(900_000L);
        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604_800_000L);
        User user = User.builder().id(1L).uuid(UUID.randomUUID()).email("a@b.com").isActive(true).condominiumRoles(List.of()).build();
        RefreshToken storedToken = RefreshToken.builder().user(user).token("oldRt").expiresAt(Instant.now().plusSeconds(3600)).revoked(false).build();
        when(refreshTokenRepository.findByToken("oldRt")).thenReturn(Optional.of(storedToken));
        when(jwtTokenProvider.generateAccessToken(any(), any(), any(), any(), any())).thenReturn("newAt");
        when(jwtTokenProvider.generateRefreshToken(any())).thenReturn("newRt");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoginResponse response = authService.refreshToken("oldRt");

        assertEquals("newAt", response.accessToken());
        assertEquals("newRt", response.refreshToken());
        verify(refreshTokenRepository).save(argThat(RefreshToken::isRevoked));
    }

    @Test
    void refreshToken_throwsWhenInvalid() {
        when(refreshTokenRepository.findByToken("invalid")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> authService.refreshToken("invalid"));
    }

    @Test
    void refreshToken_throwsWhenRevoked() {
        RefreshToken token = RefreshToken.builder().revoked(true).build();
        when(refreshTokenRepository.findByToken("rt")).thenReturn(Optional.of(token));
        assertThrows(BusinessException.class, () -> authService.refreshToken("rt"));
    }

    @Test
    void logout_deletesTokens() {
        authService.logout(1L);
        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    void logoutByUuid_deletesTokens() {
        User user = User.builder().id(1L).build();
        UUID userUuid = UUID.randomUUID();
        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.of(user));
        authService.logoutByUuid(userUuid.toString());
        verify(refreshTokenRepository).deleteByUserId(1L);
    }

    @Test
    void getCurrentUser_returnsUserInfo() {
        User user = User.builder().id(1L).uuid(UUID.randomUUID()).email("a@b.com").name("User").condominiumRoles(List.of()).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserInfoResponse response = authService.getCurrentUser(1L);
        assertEquals("a@b.com", response.email());
        assertEquals("User", response.name());
    }

    @Test
    void getCurrentUser_throwsWhenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.getCurrentUser(999L));
    }

    @Test
    void updateMyProfile_updatesUser() {
        UUID userUuid = UUID.randomUUID();
        User user = User.builder().id(1L).uuid(userUuid).email("a@b.com").name("Old").condominiumRoles(List.of()).build();
        when(userRepository.findByUuid(userUuid)).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        UserInfoResponse response = authService.updateMyProfile(userUuid.toString(), new UpdateMyProfileRequest("New Name", null));
        assertEquals("New Name", response.name());
    }

    @Test
    void forgotPassword_savesTokenWhenUserExists() {
        User user = User.builder().email("a@b.com").build();
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        authService.forgotPassword("a@b.com");
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void forgotPassword_doesNothingWhenUserNotExists() {
        when(userRepository.findByEmail("x@y.com")).thenReturn(Optional.empty());
        authService.forgotPassword("x@y.com");
        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void resetPassword_updatesPassword() {
        User user = User.builder().id(1L).build();
        PasswordResetToken token = PasswordResetToken.builder().user(user).used(false).expiresAt(Instant.now().plusSeconds(3600)).build();
        when(passwordResetTokenRepository.findByToken("t")).thenReturn(Optional.of(token));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(passwordResetTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        authService.resetPassword(new ResetPasswordRequest("t", "newPass"));
        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(user);
    }

    @Test
    void resetPassword_throwsWhenTokenUsed() {
        PasswordResetToken token = PasswordResetToken.builder().used(true).build();
        when(passwordResetTokenRepository.findByToken("t")).thenReturn(Optional.of(token));
        assertThrows(BusinessException.class, () -> authService.resetPassword(new ResetPasswordRequest("t", "p")));
    }

    @Test
    void listCondominiumsForRegistration_returnsOptions() {
        Condominium c = new Condominium();
        c.setId(1L);
        c.setName("Condo");
        when(condominiumRepository.findByStatusOrderByName("ACTIVE")).thenReturn(List.of(c));

        List<CondominiumOptionDto> result = authService.listCondominiumsForRegistration();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Condo", result.get(0).name());
    }

    @Test
    void listUnitsForRegistration_returnsOptions() {
        Condominium condo = new Condominium();
        condo.setId(1L);
        Unit unit = Unit.builder().id(1L).condominiumId(1L).identifier("101").build();
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(condo));
        when(unitRepository.findByCondominiumId(1L)).thenReturn(List.of(unit));

        List<UnitOptionDto> result = authService.listUnitsForRegistration(1L);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("101", result.get(0).identifier());
    }

    @Test
    void listUnitsForRegistration_throwsWhenCondominiumNotFound() {
        when(condominiumRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> authService.listUnitsForRegistration(999L));
    }

    @Test
    void register_createsUserAndRole() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(false);
        when(userRepository.existsByCpf(any())).thenReturn(false);
        Condominium condo = new Condominium();
        condo.setId(1L);
        Unit unit = new Unit();
        unit.setId(1L);
        unit.setCondominiumId(1L);
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(condo));
        when(unitRepository.findById(1L)).thenReturn(Optional.of(unit));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userCondominiumRoleRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        authService.register(new RegisterRequest("User", "a@b.com", "senha", "12345678901", "11999999999", 1L, 1L));

        verify(userRepository).save(any(User.class));
        verify(userCondominiumRoleRepository).save(any(UserCondominiumRole.class));
    }

    @Test
    void register_throwsWhenEmailExists() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);
        assertThrows(BusinessException.class, () ->
                authService.register(new RegisterRequest("User", "a@b.com", "senha", null, null, 1L, 1L)));
    }
}
