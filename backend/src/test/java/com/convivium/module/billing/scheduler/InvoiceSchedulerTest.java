package com.convivium.module.billing.scheduler;

import com.convivium.module.billing.entity.PlatformInvoice;
import com.convivium.module.billing.repository.PlatformInvoiceRepository;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.entity.Plan;
import com.convivium.module.condominium.repository.CondominiumRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceSchedulerTest {

    @Mock
    private CondominiumRepository condominiumRepository;

    @Mock
    private PlatformInvoiceRepository platformInvoiceRepository;

    @InjectMocks
    private InvoiceScheduler invoiceScheduler;

    @Test
    void generatePendingInvoices_createsInvoiceForActiveCondoAfter30Days() {
        Plan plan = Plan.builder().id(1L).priceCents(9900).name("Basic").build();

        Condominium condo = new Condominium();
        condo.setId(1L);
        condo.setPlan(plan);
        condo.setSubscriptionStartedAt(Instant.now().minus(35, ChronoUnit.DAYS));
        condo.setStatus("ACTIVE");

        when(condominiumRepository.findByStatusOrderByName("ACTIVE")).thenReturn(List.of(condo));
        when(platformInvoiceRepository.findByCondominiumIdAndReferenceMonth(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(platformInvoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        invoiceScheduler.generatePendingInvoices();

        verify(platformInvoiceRepository).save(any(PlatformInvoice.class));
    }

    @Test
    void generatePendingInvoices_skipsCondoWithoutPlan() {
        Condominium condo = new Condominium();
        condo.setId(1L);
        condo.setPlan(null);
        condo.setStatus("ACTIVE");

        when(condominiumRepository.findByStatusOrderByName("ACTIVE")).thenReturn(List.of(condo));

        invoiceScheduler.generatePendingInvoices();

        verify(platformInvoiceRepository, never()).save(any());
    }

    @Test
    void generatePendingInvoices_skipsCondoLessThan30Days() {
        Plan plan = Plan.builder().id(1L).priceCents(9900).name("Basic").build();

        Condominium condo = new Condominium();
        condo.setId(1L);
        condo.setPlan(plan);
        condo.setSubscriptionStartedAt(Instant.now().minus(10, ChronoUnit.DAYS));

        when(condominiumRepository.findByStatusOrderByName("ACTIVE")).thenReturn(List.of(condo));

        invoiceScheduler.generatePendingInvoices();

        verify(platformInvoiceRepository, never()).save(any());
    }

    @Test
    void generatePendingInvoices_skipsCondoWithExistingInvoice() {
        Plan plan = Plan.builder().id(1L).priceCents(9900).name("Basic").build();

        Condominium condo = new Condominium();
        condo.setId(1L);
        condo.setPlan(plan);
        condo.setSubscriptionStartedAt(Instant.now().minus(35, ChronoUnit.DAYS));

        PlatformInvoice existing = PlatformInvoice.builder().id(1L).status("PENDING").build();

        when(condominiumRepository.findByStatusOrderByName("ACTIVE")).thenReturn(List.of(condo));
        when(platformInvoiceRepository.findByCondominiumIdAndReferenceMonth(anyLong(), anyString()))
                .thenReturn(Optional.of(existing));

        invoiceScheduler.generatePendingInvoices();

        verify(platformInvoiceRepository, never()).save(any());
    }

    @Test
    void generatePendingInvoices_usesCreatedAtAsReferenceWhenNoSubscriptionStart() {
        Plan plan = Plan.builder().id(1L).priceCents(9900).name("Basic").build();

        Condominium condo = new Condominium();
        condo.setId(1L);
        condo.setPlan(plan);
        condo.setSubscriptionStartedAt(null);
        condo.setCreatedAt(Instant.now().minus(35, ChronoUnit.DAYS));

        when(condominiumRepository.findByStatusOrderByName("ACTIVE")).thenReturn(List.of(condo));
        when(platformInvoiceRepository.findByCondominiumIdAndReferenceMonth(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        when(platformInvoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        invoiceScheduler.generatePendingInvoices();

        verify(platformInvoiceRepository).save(any(PlatformInvoice.class));
    }

    @Test
    void markOverdueInvoices_marksOldPendingAsOverdue() {
        PlatformInvoice invoice = PlatformInvoice.builder()
                .id(1L)
                .status("PENDING")
                .build();
        // Simulate createdAt 20 days ago
        invoice.setCreatedAt(Instant.now().minus(20, ChronoUnit.DAYS));

        when(platformInvoiceRepository.findByStatus("PENDING")).thenReturn(List.of(invoice));
        when(platformInvoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        invoiceScheduler.markOverdueInvoices();

        assertThat(invoice.getStatus()).isEqualTo("OVERDUE");
        verify(platformInvoiceRepository).save(invoice);
    }

    @Test
    void markOverdueInvoices_doesNotMarkRecentPending() {
        PlatformInvoice invoice = PlatformInvoice.builder()
                .id(1L)
                .status("PENDING")
                .build();
        invoice.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS));

        when(platformInvoiceRepository.findByStatus("PENDING")).thenReturn(List.of(invoice));

        invoiceScheduler.markOverdueInvoices();

        assertThat(invoice.getStatus()).isEqualTo("PENDING");
        verify(platformInvoiceRepository, never()).save(any());
    }

    @Test
    void blockDelinquentCondominiums_blocksCondoWithOldOverdueInvoice() {
        Condominium condo = new Condominium();
        condo.setId(1L);
        condo.setBlockType(null);

        PlatformInvoice invoice = PlatformInvoice.builder()
                .id(1L)
                .status("OVERDUE")
                .condominium(condo)
                .build();
        invoice.setCreatedAt(Instant.now().minus(20, ChronoUnit.DAYS));

        when(platformInvoiceRepository.findByStatus("OVERDUE")).thenReturn(List.of(invoice));
        when(condominiumRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        invoiceScheduler.blockDelinquentCondominiums();

        assertThat(condo.getBlockType()).isEqualTo("PAYMENT");
        assertThat(condo.getBlockedAt()).isNotNull();
        assertThat(condo.getBlockedReason()).contains("inadimplencia");
        verify(condominiumRepository).save(condo);
    }

    @Test
    void blockDelinquentCondominiums_doesNotBlockAlreadyBlockedCondo() {
        Condominium condo = new Condominium();
        condo.setId(1L);
        condo.setBlockType("PAYMENT");

        PlatformInvoice invoice = PlatformInvoice.builder()
                .id(1L)
                .status("OVERDUE")
                .condominium(condo)
                .build();
        invoice.setCreatedAt(Instant.now().minus(20, ChronoUnit.DAYS));

        when(platformInvoiceRepository.findByStatus("OVERDUE")).thenReturn(List.of(invoice));

        invoiceScheduler.blockDelinquentCondominiums();

        verify(condominiumRepository, never()).save(any());
    }

    @Test
    void blockDelinquentCondominiums_doesNotBlockForRecentOverdue() {
        Condominium condo = new Condominium();
        condo.setId(1L);
        condo.setBlockType(null);

        PlatformInvoice invoice = PlatformInvoice.builder()
                .id(1L)
                .status("OVERDUE")
                .condominium(condo)
                .build();
        invoice.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS));

        when(platformInvoiceRepository.findByStatus("OVERDUE")).thenReturn(List.of(invoice));

        invoiceScheduler.blockDelinquentCondominiums();

        assertThat(condo.getBlockType()).isNull();
        verify(condominiumRepository, never()).save(any());
    }

    @Test
    void processInvoices_callsAllThreeMethods() {
        when(condominiumRepository.findByStatusOrderByName("ACTIVE")).thenReturn(List.of());
        when(platformInvoiceRepository.findByStatus("PENDING")).thenReturn(List.of());
        when(platformInvoiceRepository.findByStatus("OVERDUE")).thenReturn(List.of());

        invoiceScheduler.processInvoices();

        verify(condominiumRepository).findByStatusOrderByName("ACTIVE");
        verify(platformInvoiceRepository).findByStatus("PENDING");
        verify(platformInvoiceRepository).findByStatus("OVERDUE");
    }
}
