package com.convivium.module.billing.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.common.exception.BusinessException;
import com.convivium.module.billing.service.PlatformInvoiceService;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.repository.CondominiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/billing")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
@RequiredArgsConstructor
public class AdminBillingController {

    private final CondominiumRepository condominiumRepository;
    private final PlatformInvoiceService platformInvoiceService;

    /**
     * Lista todos os condominios com informacoes de billing.
     */
    @GetMapping("/condominiums")
    public ResponseEntity<ApiResponse<List<CondoBillingDto>>> listCondominiums() {
        List<Condominium> condos = condominiumRepository.findAll();
        List<CondoBillingDto> result = condos.stream()
                .map(c -> new CondoBillingDto(
                        c.getId(),
                        c.getName(),
                        c.getSlug(),
                        c.getPlan() != null ? c.getPlan().getName() : null,
                        c.getPlan() != null ? c.getPlan().getPriceCents() : null,
                        c.getStatus(),
                        c.getBlockType(),
                        c.getBlockedAt(),
                        c.getBlockedReason(),
                        c.getSubscriptionStartedAt(),
                        c.getCreatedAt()
                ))
                .toList();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * Bloqueia um condominio (PAYMENT ou GENERAL).
     */
    @PatchMapping("/condominiums/{id}/block")
    public ResponseEntity<ApiResponse<Void>> blockCondominium(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String blockType = body.get("blockType");
        if (blockType == null || (!blockType.equals("PAYMENT") && !blockType.equals("GENERAL"))) {
            throw new BusinessException("blockType deve ser PAYMENT ou GENERAL", "INVALID_BLOCK_TYPE");
        }

        Condominium condo = condominiumRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Condominio nao encontrado", "CONDOMINIUM_NOT_FOUND"));

        condo.setBlockType(blockType);
        condo.setBlockedAt(Instant.now());
        condo.setBlockedReason(body.getOrDefault("reason", "Bloqueado pelo administrador"));
        condominiumRepository.save(condo);

        return ResponseEntity.ok(ApiResponse.ok(null, "Condominio bloqueado com sucesso"));
    }

    /**
     * Desbloqueia um condominio (remove o blockType).
     */
    @PatchMapping("/condominiums/{id}/unblock")
    public ResponseEntity<ApiResponse<Void>> unblockCondominium(@PathVariable Long id) {
        Condominium condo = condominiumRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Condominio nao encontrado", "CONDOMINIUM_NOT_FOUND"));

        condo.setBlockType(null);
        condo.setBlockedAt(null);
        condo.setBlockedReason(null);
        condominiumRepository.save(condo);

        return ResponseEntity.ok(ApiResponse.ok(null, "Condominio desbloqueado com sucesso"));
    }

    /**
     * Lista faturas de um condominio especifico.
     */
    @GetMapping("/condominiums/{id}/invoices")
    public ResponseEntity<ApiResponse<List<PlatformInvoiceService.PlatformInvoiceDto>>> listInvoices(
            @PathVariable Long id) {
        List<PlatformInvoiceService.PlatformInvoiceDto> list = platformInvoiceService.listByCondominium(id, 100);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    public record CondoBillingDto(
            Long id,
            String name,
            String slug,
            String planName,
            Integer planPriceCents,
            String status,
            String blockType,
            Instant blockedAt,
            String blockedReason,
            Instant subscriptionStartedAt,
            Instant createdAt
    ) {}
}
