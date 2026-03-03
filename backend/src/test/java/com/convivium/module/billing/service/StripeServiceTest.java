package com.convivium.module.billing.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.config.StripeProperties;
import com.convivium.module.billing.entity.PlatformInvoice;
import com.convivium.module.billing.repository.PlatformInvoiceRepository;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.entity.Plan;
import com.convivium.module.condominium.repository.CondominiumRepository;
import com.convivium.module.condominium.repository.PlanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StripeServiceTest {

    @Mock
    private StripeProperties stripeProperties;
    @Mock
    private CondominiumRepository condominiumRepository;
    @Mock
    private PlanRepository planRepository;
    @Mock
    private PlatformInvoiceRepository platformInvoiceRepository;

    @InjectMocks
    private StripeService stripeService;

    @Test
    void createCheckoutSession_stripeNotEnabled_throws() {
        when(stripeProperties.isEnabled()).thenReturn(false);

        assertThatThrownBy(() -> stripeService.createCheckoutSession(1L, 1L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stripe nao estao configurados");
    }

    @Test
    void createCheckoutSession_condoNotFound_throws() {
        when(stripeProperties.isEnabled()).thenReturn(true);
        when(stripeProperties.getSecretKey()).thenReturn("sk_test_123");
        when(condominiumRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stripeService.createCheckoutSession(99L, 1L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Condominio nao encontrado");
    }

    @Test
    void createCheckoutSession_planNotFound_throws() {
        when(stripeProperties.isEnabled()).thenReturn(true);
        when(stripeProperties.getSecretKey()).thenReturn("sk_test_123");

        Condominium condo = new Condominium();
        condo.setId(1L);
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(condo));
        when(planRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stripeService.createCheckoutSession(1L, 99L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Plano nao encontrado");
    }

    @Test
    void createCheckoutSession_planMismatch_throws() {
        when(stripeProperties.isEnabled()).thenReturn(true);
        when(stripeProperties.getSecretKey()).thenReturn("sk_test_123");

        Plan condoPlan = new Plan();
        condoPlan.setId(1L);
        Condominium condo = new Condominium();
        condo.setId(1L);
        condo.setPlan(condoPlan);
        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(condo));

        Plan otherPlan = new Plan();
        otherPlan.setId(2L);
        when(planRepository.findById(2L)).thenReturn(Optional.of(otherPlan));

        assertThatThrownBy(() -> stripeService.createCheckoutSession(1L, 2L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("plano informado nao e o plano atual");
    }

    @Test
    void createCheckoutSession_invoiceAlreadyPaid_throws() {
        when(stripeProperties.isEnabled()).thenReturn(true);
        when(stripeProperties.getSecretKey()).thenReturn("sk_test_123");

        Plan plan = Plan.builder().id(1L).priceCents(9900).name("Basic").build();
        Condominium condo = new Condominium();
        condo.setId(1L);
        condo.setPlan(plan);

        when(condominiumRepository.findById(1L)).thenReturn(Optional.of(condo));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));

        PlatformInvoice paidInvoice = PlatformInvoice.builder()
                .id(10L)
                .status("PAID")
                .build();
        when(platformInvoiceRepository.findByCondominiumIdAndReferenceMonth(any(), any()))
                .thenReturn(Optional.of(paidInvoice));

        assertThatThrownBy(() -> stripeService.createCheckoutSession(1L, 1L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ja foi paga");
    }

    @Test
    void handleCheckoutSessionCompleted_marksInvoiceAsPaid() {
        PlatformInvoice inv = PlatformInvoice.builder()
                .id(1L)
                .status("PENDING")
                .build();
        when(platformInvoiceRepository.findByStripeSessionId("sess_123")).thenReturn(Optional.of(inv));
        when(platformInvoiceRepository.save(any())).thenReturn(inv);

        stripeService.handleCheckoutSessionCompleted("sess_123");

        assertThat(inv.getStatus()).isEqualTo("PAID");
        assertThat(inv.getPaidAt()).isNotNull();
        verify(platformInvoiceRepository).save(inv);
    }

    @Test
    void handleCheckoutSessionCompleted_sessionNotFound_doesNothing() {
        when(platformInvoiceRepository.findByStripeSessionId("unknown")).thenReturn(Optional.empty());

        stripeService.handleCheckoutSessionCompleted("unknown");
        // No exception, no interaction with save
    }
}
