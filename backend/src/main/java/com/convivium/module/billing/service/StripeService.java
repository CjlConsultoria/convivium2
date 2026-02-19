package com.convivium.module.billing.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.config.StripeProperties;
import com.convivium.module.billing.entity.PlatformInvoice;
import com.convivium.module.billing.repository.PlatformInvoiceRepository;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.entity.Plan;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.condominium.repository.PlanRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    private static final DateTimeFormatter REF_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    private final StripeProperties stripeProperties;
    private final CondominiumRepository condominiumRepository;
    private final PlanRepository planRepository;
    private final PlatformInvoiceRepository platformInvoiceRepository;

    /**
     * Cria uma sessão de Checkout Stripe para pagamento da mensalidade (um único pagamento).
     * Métodos habilitados: cartão, Apple Pay, Google Pay, Link, Boleto (conforme configuração Stripe).
     * Pix não é incluído (em breve).
     */
    @Transactional
    public String createCheckoutSession(Long condominiumId, Long planId, boolean redirect) {
        if (!stripeProperties.isEnabled() || stripeProperties.getSecretKey() == null || stripeProperties.getSecretKey().isBlank()) {
            throw new BusinessException("Pagamentos Stripe nao estao configurados.", "STRIPE_NOT_CONFIGURED");
        }

        Condominium condo = condominiumRepository.findById(condominiumId)
                .orElseThrow(() -> new BusinessException("Condominio nao encontrado", "CONDOMINIUM_NOT_FOUND"));
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new BusinessException("Plano nao encontrado", "PLAN_NOT_FOUND"));

        if (condo.getPlan() == null || !condo.getPlan().getId().equals(planId)) {
            throw new BusinessException("O plano informado nao e o plano atual do condominio.", "PLAN_MISMATCH");
        }

        String referenceMonth = YearMonth.now().format(REF_MONTH);
        PlatformInvoice invoice = platformInvoiceRepository
                .findByCondominiumIdAndReferenceMonth(condominiumId, referenceMonth)
                .orElseGet(() -> {
                    PlatformInvoice newInv = PlatformInvoice.builder()
                            .condominium(condo)
                            .plan(plan)
                            .referenceMonth(referenceMonth)
                            .amountCents(plan.getPriceCents())
                            .status("PENDING")
                            .build();
                    return platformInvoiceRepository.save(newInv);
                });

        if ("PAID".equals(invoice.getStatus())) {
            throw new BusinessException("Esta fatura ja foi paga.", "INVOICE_ALREADY_PAID");
        }

        String successUrl = stripeProperties.getSuccessUrl().replace("{condoId}", String.valueOf(condominiumId));
        String cancelUrl = stripeProperties.getCancelUrl().replace("{condoId}", String.valueOf(condominiumId));

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.BOLETO)
                .putMetadata("condominiumId", String.valueOf(condominiumId))
                .putMetadata("planId", String.valueOf(planId))
                .putMetadata("referenceMonth", referenceMonth)
                .putMetadata("platformInvoiceId", String.valueOf(invoice.getId()))
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("brl")
                                                .setUnitAmount((long) plan.getPriceCents())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Mensalidade Convivium - " + plan.getName())
                                                                .setDescription("Referencia " + referenceMonth)
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        try {
            com.stripe.Stripe.apiKey = stripeProperties.getSecretKey();
            Session session = Session.create(params);
            invoice.setStripeSessionId(session.getId());
            platformInvoiceRepository.save(invoice);
            return session.getUrl();
        } catch (StripeException e) {
            log.error("Stripe checkout session creation failed", e);
            throw new BusinessException("Nao foi possivel criar a sessao de pagamento: " + e.getMessage(), "STRIPE_ERROR");
        }
    }

    /**
     * Processa evento do webhook (checkout.session.completed ou invoice.paid) e marca fatura como paga.
     */
    @Transactional
    public void handleCheckoutSessionCompleted(String sessionId) {
        platformInvoiceRepository.findByStripeSessionId(sessionId).ifPresent(inv -> {
            inv.setStatus("PAID");
            inv.setPaidAt(java.time.Instant.now());
            platformInvoiceRepository.save(inv);
            log.info("Platform invoice {} marked as PAID (session {})", inv.getId(), sessionId);
        });
    }
}
