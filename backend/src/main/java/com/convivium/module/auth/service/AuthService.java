package com.convivium.module.auth.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.common.exception.ResourceNotFoundException;
import com.convivium.common.util.CpfUtil;
import com.convivium.module.auth.dto.CondominiumOptionDto;
import com.convivium.module.auth.dto.CondominiumRoleResponse;
import com.convivium.module.auth.dto.GoogleAuthResponse;
import com.convivium.module.auth.dto.LoginRequest;
import com.convivium.module.auth.dto.LoginResponse;
import com.convivium.module.auth.dto.RegisterRequest;
import com.convivium.module.auth.dto.PendingRegistrationStatusResponse;
import com.convivium.module.auth.dto.RegisterGoogleRequest;
import com.convivium.module.auth.dto.RegisterGoogleResponse;
import com.convivium.module.auth.dto.UnitOptionDto;
import com.convivium.module.auth.dto.ResetPasswordRequest;
import com.convivium.module.auth.dto.UpdateMyProfileRequest;
import com.convivium.module.auth.dto.UserInfoResponse;
import com.convivium.integration.google.GoogleIdTokenVerifier;
import com.convivium.integration.google.GoogleIdTokenVerifier.GoogleUserInfo;
import com.convivium.integration.google.GoogleIdTokenVerifier.InvalidGoogleTokenException;
import com.convivium.module.auth.entity.PasswordResetToken;
import com.convivium.module.auth.entity.RefreshToken;
import com.convivium.module.auth.repository.PasswordResetTokenRepository;
import com.convivium.module.auth.repository.RefreshTokenRepository;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.entity.Unit;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.condominium.repository.UnitRepository;
import com.convivium.module.user.entity.Role;
import com.convivium.module.user.entity.User;
import com.convivium.module.user.entity.UserCondominiumRole;
import com.convivium.module.user.repository.UserCondominiumRoleRepository;
import com.convivium.module.user.repository.UserRepository;
import com.convivium.security.jwt.JwtProperties;
import com.convivium.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final UserCondominiumRoleRepository userCondominiumRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final CondominiumRepository condominiumRepository;
    private final UnitRepository unitRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Email ou senha invalidos", "INVALID_CREDENTIALS"));

        if (user.getPasswordHash() == null) {
            throw new BusinessException("Use o login com Google para esta conta.", "USE_GOOGLE_LOGIN");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Email ou senha invalidos", "INVALID_CREDENTIALS");
        }

        if (!user.isActive()) {
            throw new BusinessException("Conta desativada. Entre em contato com o administrador.", "ACCOUNT_DISABLED");
        }

        List<UserCondominiumRole> roles = user.getCondominiumRoles();

        Long condominiumId = null;
        List<String> roleNames = new ArrayList<>();
        List<String> permissions = Collections.emptyList();

        if (!roles.isEmpty()) {
            UserCondominiumRole firstActive = roles.stream()
                    .filter(r -> "ACTIVE".equals(r.getStatus()))
                    .findFirst()
                    .orElse(roles.get(0));
            condominiumId = firstActive.getCondominium().getId();
            roleNames.add(firstActive.getRole().name());
        }

        if (user.isPlatformAdmin()) {
            roleNames.add("PLATFORM_ADMIN");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUuid().toString(),
                user.getEmail(),
                condominiumId,
                roleNames,
                permissions
        );

        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getUuid().toString());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        UserInfoResponse userInfo = buildUserInfoResponse(user);

        return new LoginResponse(
                accessToken,
                refreshTokenStr,
                jwtProperties.getAccessTokenExpiration() / 1000,
                userInfo
        );
    }

    @Transactional(readOnly = true)
    public List<CondominiumOptionDto> listCondominiumsForRegistration() {
        return condominiumRepository.findByStatusOrderByName("ACTIVE").stream()
                .map(c -> new CondominiumOptionDto(c.getId(), c.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UnitOptionDto> listUnitsForRegistration(Long condominiumId) {
        Condominium condominium = condominiumRepository.findById(condominiumId)
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", condominiumId));
        return unitRepository.findByCondominiumId(condominiumId).stream()
                .map(u -> new UnitOptionDto(
                        u.getId(),
                        u.getIdentifier(),
                        u.getBuilding() != null ? u.getBuilding().getName() : null))
                .toList();
    }

    /** Login com Google: verifica token; se usuário existe e tem condomínio ativo, retorna tokens; senão retorna needsRegistration. */
    public GoogleAuthResponse googleLogin(String idToken) {
        GoogleUserInfo info = googleIdTokenVerifier.verify(idToken);
        Optional<User> optUser = userRepository.findByEmail(info.email());
        if (optUser.isEmpty()) {
            return GoogleAuthResponse.needsRegistration(info.email(), info.name(), info.picture());
        }
        User user = optUser.get();
        if (!user.isActive()) {
            throw new BusinessException("Conta desativada.", "ACCOUNT_DISABLED");
        }
        List<UserCondominiumRole> roles = user.getCondominiumRoles().stream()
                .filter(r -> "ACTIVE".equals(r.getStatus()))
                .toList();
        if (roles.isEmpty()) {
            return GoogleAuthResponse.needsRegistration(info.email(), user.getName(), user.getPhotoUrl());
        }
        LoginResponse login = buildLoginResponseForUser(user);
        return GoogleAuthResponse.loggedIn(login);
    }

    /** Verifica se o e-mail (via idToken) já possui cadastro pendente de aprovação em algum condomínio. */
    @Transactional(readOnly = true)
    public PendingRegistrationStatusResponse checkPendingRegistration(String idToken) {
        GoogleUserInfo info = googleIdTokenVerifier.verify(idToken);
        User user = userRepository.findByEmail(info.email()).orElse(null);
        if (user == null) {
            return PendingRegistrationStatusResponse.noPending();
        }
        Optional<UserCondominiumRole> pending = user.getCondominiumRoles().stream()
                .filter(r -> "PENDING_APPROVAL".equals(r.getStatus()))
                .findFirst();
        if (pending.isEmpty()) {
            return PendingRegistrationStatusResponse.noPending();
        }
        String condoName = pending.get().getCondominium() != null ? pending.get().getCondominium().getName() : null;
        String message = "Este e-mail já possui um cadastro pendente de aprovação"
                + (condoName != null ? " no condomínio " + condoName : "")
                + ". Aguarde o síndico ou administrador aprovar. Não é necessário preencher o formulário novamente.";
        return PendingRegistrationStatusResponse.pending(message, condoName);
    }

    /** Completa cadastro do morador após login com Google: condomínio, unidade, telefone (opcional). Cria vínculo como PENDING_APPROVAL; só retorna login se já tiver vínculo ativo nesse condomínio. */
    public RegisterGoogleResponse registerGoogle(RegisterGoogleRequest request) {
        GoogleUserInfo info = googleIdTokenVerifier.verify(request.idToken());
        Condominium condominium = condominiumRepository.findById(request.condominiumId())
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", request.condominiumId()));
        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidade", request.unitId()));
        if (!unit.getCondominiumId().equals(condominium.getId())) {
            throw new BusinessException("Unidade nao pertence ao condominio selecionado", "UNIT_MISMATCH");
        }

        User user = userRepository.findByEmail(info.email()).orElse(null);
        if (user == null) {
            user = User.builder()
                    .email(info.email())
                    .passwordHash(null)
                    .name(info.name())
                    .photoUrl(info.picture())
                    .phone(request.phone())
                    .isPlatformAdmin(false)
                    .isActive(true)
                    .emailVerified(true)
                    .build();
            user = userRepository.save(user);
        } else {
            if (user.getPhone() == null && request.phone() != null) {
                user.setPhone(request.phone());
            }
            if (user.getPhotoUrl() == null && info.picture() != null) {
                user.setPhotoUrl(info.picture());
            }
            userRepository.save(user);
        }

        var existingInCondo = user.getCondominiumRoles().stream()
                .filter(r -> r.getCondominium().getId().equals(condominium.getId()))
                .findFirst();
        if (existingInCondo.isPresent()) {
            UserCondominiumRole ucr = existingInCondo.get();
            if ("ACTIVE".equals(ucr.getStatus())) {
                return RegisterGoogleResponse.loggedIn(buildLoginResponseForUser(user));
            }
            return RegisterGoogleResponse.pendingApproval("Seu cadastro neste condomínio já está pendente de aprovação. Aguarde o síndico ou administrador aprovar.");
        }

        UserCondominiumRole role = UserCondominiumRole.builder()
                .user(user)
                .condominium(condominium)
                .unit(unit)
                .role(Role.MORADOR)
                .status("PENDING_APPROVAL")
                .build();
        userCondominiumRoleRepository.save(role);
        user.getCondominiumRoles().add(role);

        return RegisterGoogleResponse.pendingApproval("Cadastro enviado! Aguarde a aprovação do síndico ou administrador para acessar o condomínio.");
    }

    private LoginResponse buildLoginResponseForUser(User user) {
        List<UserCondominiumRole> roles = user.getCondominiumRoles();
        Long condominiumId = null;
        List<String> roleNames = new ArrayList<>();
        if (!roles.isEmpty()) {
            UserCondominiumRole firstActive = roles.stream()
                    .filter(r -> "ACTIVE".equals(r.getStatus()))
                    .findFirst()
                    .orElse(roles.get(0));
            condominiumId = firstActive.getCondominium().getId();
            roleNames.add(firstActive.getRole().name());
        }
        if (user.isPlatformAdmin()) {
            roleNames.add("PLATFORM_ADMIN");
        }
        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getUuid().toString(),
                user.getEmail(),
                condominiumId,
                roleNames,
                Collections.emptyList());
        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getUuid().toString());
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        UserInfoResponse userInfo = buildUserInfoResponse(user);
        return new LoginResponse(
                accessToken,
                refreshTokenStr,
                jwtProperties.getAccessTokenExpiration() / 1000,
                userInfo);
    }

    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email ja cadastrado", "EMAIL_ALREADY_EXISTS");
        }

        String cpfNormalized = CpfUtil.normalize(request.cpf());
        if (cpfNormalized != null && userRepository.existsByCpf(cpfNormalized)) {
            throw new BusinessException("CPF ja cadastrado", "CPF_ALREADY_EXISTS");
        }

        Condominium condominium = condominiumRepository.findById(request.condominiumId())
                .orElseThrow(() -> new ResourceNotFoundException("Condominio", request.condominiumId()));

        Unit unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unidade", request.unitId()));

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .name(request.name())
                .cpf(cpfNormalized)
                .phone(request.phone())
                .isPlatformAdmin(false)
                .isActive(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        UserCondominiumRole userRole = UserCondominiumRole.builder()
                .user(user)
                .condominium(condominium)
                .unit(unit)
                .role(Role.MORADOR)
                .status("PENDING_APPROVAL")
                .build();

        userCondominiumRoleRepository.save(userRole);
    }

    public LoginResponse refreshToken(String refreshTokenStr) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BusinessException("Refresh token invalido", "INVALID_REFRESH_TOKEN"));

        if (storedToken.isRevoked()) {
            throw new BusinessException("Refresh token revogado", "REFRESH_TOKEN_REVOKED");
        }

        if (storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token expirado", "REFRESH_TOKEN_EXPIRED");
        }

        User user = storedToken.getUser();

        List<UserCondominiumRole> roles = user.getCondominiumRoles();

        Long condominiumId = null;
        List<String> roleNames = new ArrayList<>();
        List<String> permissions = Collections.emptyList();

        if (!roles.isEmpty()) {
            UserCondominiumRole firstActive = roles.stream()
                    .filter(r -> "ACTIVE".equals(r.getStatus()))
                    .findFirst()
                    .orElse(roles.get(0));
            condominiumId = firstActive.getCondominium().getId();
            roleNames.add(firstActive.getRole().name());
        }

        if (user.isPlatformAdmin()) {
            roleNames.add("PLATFORM_ADMIN");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getUuid().toString(),
                user.getEmail(),
                condominiumId,
                roleNames,
                permissions
        );

        String newRefreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getUuid().toString());

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .user(user)
                .token(newRefreshTokenStr)
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration()))
                .revoked(false)
                .build();
        refreshTokenRepository.save(newRefreshToken);

        UserInfoResponse userInfo = buildUserInfoResponse(user);

        return new LoginResponse(
                newAccessToken,
                newRefreshTokenStr,
                jwtProperties.getAccessTokenExpiration() / 1000,
                userInfo
        );
    }

    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public void logoutByUuid(String uuid) {
        User user = userRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado", "USER_NOT_FOUND"));
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", userId));
        return buildUserInfoResponse(user);
    }

    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUserByUuid(String uuid) {
        User user = userRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado", "USER_NOT_FOUND"));
        return buildUserInfoResponse(user);
    }

    @Transactional
    public UserInfoResponse updateMyProfile(String uuid, UpdateMyProfileRequest request) {
        User user = userRepository.findByUuid(UUID.fromString(uuid))
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado", "USER_NOT_FOUND"));
        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        user = userRepository.save(user);
        return buildUserInfoResponse(user);
    }

    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            // TODO: Integrar com servico de email
            log.info("Password reset token gerado para usuario {}: {}", user.getEmail(), token);
        });
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BusinessException("Token invalido", "INVALID_RESET_TOKEN"));

        if (resetToken.isUsed()) {
            throw new BusinessException("Token ja utilizado", "RESET_TOKEN_USED");
        }

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessException("Token expirado", "RESET_TOKEN_EXPIRED");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    private UserInfoResponse buildUserInfoResponse(User user) {
        List<CondominiumRoleResponse> roleResponses = user.getCondominiumRoles().stream()
                .map(ucr -> {
                    Condominium condo = ucr.getCondominium();
                    Unit unit = ucr.getUnit();
                    return new CondominiumRoleResponse(
                            condo.getId(),
                            condo.getName(),
                            ucr.getRole().name(),
                            ucr.getStatus() != null ? ucr.getStatus() : "ACTIVE",
                            unit != null ? unit.getId() : null,
                            unit != null ? unit.getIdentifier() : null
                    );
                })
                .toList();

        return new UserInfoResponse(
                user.getId(),
                user.getUuid().toString(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getPhotoUrl(),
                user.isPlatformAdmin(),
                roleResponses
        );
    }
}
