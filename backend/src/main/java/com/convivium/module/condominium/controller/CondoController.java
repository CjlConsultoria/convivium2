package com.convivium.module.condominium.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.condominium.dto.CondominiumResponse;
import com.convivium.module.condominium.dto.PlanResponse;
import com.convivium.module.condominium.service.CondominiumService;
import com.convivium.security.CurrentUser;
import com.convivium.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    /** Lista planos ativos disponiveis para selecao pelo sindico. */
    @GetMapping("/{condoId}/plans")
    public ResponseEntity<ApiResponse<List<PlanResponse>>> listAvailablePlans(
            @PathVariable Long condoId) {
        List<PlanResponse> plans = condominiumService.listAvailablePlans();
        return ResponseEntity.ok(ApiResponse.ok(plans));
    }

    /** Sindico seleciona/altera o plano do condominio. */
    @PatchMapping("/{condoId}/plan")
    @PreAuthorize("hasRole('SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<CondominiumResponse>> selectPlan(
            @PathVariable Long condoId,
            @RequestBody Map<String, Long> body) {
        Long planId = body.get("planId");
        CondominiumResponse response = condominiumService.setPlan(condoId, planId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
