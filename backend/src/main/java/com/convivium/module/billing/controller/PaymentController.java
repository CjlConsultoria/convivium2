package com.convivium.module.billing.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.billing.service.PlatformInvoiceService;
import com.convivium.module.billing.service.StripeService;
import com.convivium.module.condominium.repository.CondominiumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/condos/{condoId}/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final StripeService stripeService;
    private final PlatformInvoiceService platformInvoiceService;
    private final CondominiumRepository condominiumRepository;

    /**
     * Cria uma sessão de Checkout Stripe e retorna a URL para redirecionamento.
     * Corpo opcional: { "planId": 1 }. Se não enviar, usa o plano atual do condomínio.
     * Métodos na sessão: cartão, Apple Pay, Google Pay, Link, Boleto. Pix em breve.
     */
    @PostMapping("/checkout-session")
    @PreAuthorize("hasRole('SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, String>>> createCheckoutSession(
            @PathVariable Long condoId,
            @RequestBody(required = false) Map<String, Object> body) {

        Long planId = null;
        if (body != null && body.get("planId") != null) {
            planId = Long.valueOf(body.get("planId").toString());
        }
        if (planId == null) {
            planId = condominiumRepository.findById(condoId)
                    .filter(c -> c.getPlan() != null)
                    .map(c -> c.getPlan().getId())
                    .orElse(null);
        }
        if (planId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Condominio sem plano associado. Informe planId ou associe um plano.", "MISSING_PLAN"));
        }

        String url = stripeService.createCheckoutSession(condoId, planId, true);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("url", url)));
    }

    /**
     * Lista as faturas da plataforma (mensalidade do plano) do condomínio.
     * Atualizadas via webhook Stripe quando o pagamento é confirmado.
     */
    @GetMapping("/invoices")
    @PreAuthorize("hasRole('SINDICO') or hasRole('PLATFORM_ADMIN')")
    public ResponseEntity<ApiResponse<List<PlatformInvoiceService.PlatformInvoiceDto>>> listInvoices(
            @PathVariable Long condoId) {

        List<PlatformInvoiceService.PlatformInvoiceDto> list = platformInvoiceService.listByCondominium(condoId, 24);
        return ResponseEntity.ok(ApiResponse.ok(list));
    }
}
