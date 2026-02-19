package com.convivium.module.condominium.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.condominium.dto.CondominiumResponse;
import com.convivium.module.condominium.service.CondominiumService;
import com.convivium.security.CurrentUser;
import com.convivium.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints para o contexto do condomínio (tenant).
 * O acesso ao condoId é validado pelo TenantFilter (usuário deve pertencer ao condomínio ou ser PLATFORM_ADMIN).
 */
@RestController
@RequestMapping("/api/v1/condos")
@RequiredArgsConstructor
public class CondoController {

    private final CondominiumService condominiumService;

    /** Retorna os dados do condomínio, incluindo plano e valor do plano (para síndico/financeiro, etc.). */
    @GetMapping("/{condoId}")
    public ResponseEntity<ApiResponse<CondominiumResponse>> getCondo(
            @PathVariable Long condoId,
            @CurrentUser UserPrincipal currentUser) {

        CondominiumResponse response = condominiumService.getById(condoId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
