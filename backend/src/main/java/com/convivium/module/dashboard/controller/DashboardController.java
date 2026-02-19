package com.convivium.module.dashboard.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.common.dto.PageResponse;
import com.convivium.module.dashboard.dto.DashboardStatsResponse;
import com.convivium.module.dashboard.dto.UnitActivityItemDto;
import com.convivium.module.dashboard.service.DashboardService;
import com.convivium.security.CurrentUser;
import com.convivium.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/condos/{condoId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Estatísticas do painel (total moradores, denúncias abertas, encomendas pendentes, reservas hoje).
     * Acessível a qualquer usuário do condomínio (Síndico, Sub, Porteiro, Morador).
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO', 'MORADOR')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats(
            @PathVariable Long condoId,
            @CurrentUser UserPrincipal currentUser) {
        DashboardStatsResponse stats = dashboardService.getStats(condoId, currentUser);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    /**
     * Últimos 30 dias de atividades da unidade (denúncias e encomendas), paginado.
     */
    @GetMapping("/activity")
    @PreAuthorize("hasAnyRole('SINDICO', 'SUB_SINDICO', 'PORTEIRO', 'MORADOR')")
    public ResponseEntity<ApiResponse<PageResponse<UnitActivityItemDto>>> getUnitActivity(
            @PathVariable Long condoId,
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<UnitActivityItemDto> activity = dashboardService.getUnitActivity(condoId, currentUser, page, size);
        return ResponseEntity.ok(ApiResponse.ok(activity));
    }
}
