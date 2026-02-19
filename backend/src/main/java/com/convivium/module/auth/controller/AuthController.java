package com.convivium.module.auth.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.auth.dto.CheckPendingRegistrationRequest;
import com.convivium.module.auth.dto.ForgotPasswordRequest;
import com.convivium.module.auth.dto.GoogleAuthResponse;
import com.convivium.module.auth.dto.PendingRegistrationStatusResponse;
import com.convivium.module.auth.dto.GoogleLoginRequest;
import com.convivium.module.auth.dto.LoginRequest;
import com.convivium.module.auth.dto.LoginResponse;
import com.convivium.module.auth.dto.RegisterGoogleResponse;
import com.convivium.module.auth.dto.CondominiumOptionDto;
import com.convivium.module.auth.dto.RefreshTokenRequest;
import com.convivium.module.auth.dto.RegisterGoogleRequest;
import com.convivium.module.auth.dto.RegisterRequest;
import com.convivium.module.auth.dto.ResetPasswordRequest;
import com.convivium.module.auth.dto.UnitOptionDto;
import com.convivium.module.auth.dto.UpdateMyProfileRequest;
import com.convivium.module.auth.dto.UserInfoResponse;
import com.convivium.module.auth.service.AuthService;
import com.convivium.security.CurrentUser;
import com.convivium.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** Login com Google: retorna tokens ou needsRegistration (email, name, picture) para completar cadastro. */
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<GoogleAuthResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        GoogleAuthResponse response = authService.googleLogin(request.idToken());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** Verifica se este e-mail já tem cadastro pendente de aprovação (para não exibir o formulário de novo). */
    @PostMapping("/check-pending-registration")
    public ResponseEntity<ApiResponse<PendingRegistrationStatusResponse>> checkPendingRegistration(
            @Valid @RequestBody CheckPendingRegistrationRequest request) {
        PendingRegistrationStatusResponse response = authService.checkPendingRegistration(request.idToken());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** Completa cadastro do morador após Google (condomínio, unidade, telefone). Retorna needsApproval quando ficar pendente de aprovação. */
    @PostMapping("/register-google")
    public ResponseEntity<ApiResponse<RegisterGoogleResponse>> registerGoogle(@Valid @RequestBody RegisterGoogleRequest request) {
        RegisterGoogleResponse response = authService.registerGoogle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, response.needsApproval() ? "Aguardando aprovação." : "Cadastro concluído."));
    }

    /** Lista condominios ativos para o formulario de cadastro (publico). */
    @GetMapping("/condominiums")
    public ResponseEntity<ApiResponse<List<CondominiumOptionDto>>> listCondominiumsForRegistration() {
        List<CondominiumOptionDto> list = authService.listCondominiumsForRegistration();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    /** Lista unidades de um condominio para o formulario de cadastro (publico). */
    @GetMapping("/condominiums/{condominiumId}/units")
    public ResponseEntity<ApiResponse<List<UnitOptionDto>>> listUnitsForRegistration(
            @PathVariable Long condominiumId) {
        List<UnitOptionDto> list = authService.listUnitsForRegistration(condominiumId);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(null, "Registro realizado. Aguarde aprovacao do sindico."));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CurrentUser UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Nao autenticado."));
        }
        authService.logoutByUuid(currentUser.getUuid());
        return ResponseEntity.ok(ApiResponse.ok(null, "Logout realizado."));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> me(@CurrentUser UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Token invalido ou expirado."));
        }
        UserInfoResponse userInfo = authService.getCurrentUserByUuid(currentUser.getUuid());
        return ResponseEntity.ok(ApiResponse.ok(userInfo));
    }

    /** Atualiza dados do próprio perfil (nome, telefone). E-mail e CPF não podem ser alterados. */
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateMyProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UpdateMyProfileRequest request) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("Nao autenticado."));
        }
        UserInfoResponse userInfo = authService.updateMyProfile(currentUser.getUuid(), request);
        return ResponseEntity.ok(ApiResponse.ok(userInfo, "Perfil atualizado."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok(ApiResponse.ok(null, "Email enviado."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(null, "Senha alterada."));
    }
}
