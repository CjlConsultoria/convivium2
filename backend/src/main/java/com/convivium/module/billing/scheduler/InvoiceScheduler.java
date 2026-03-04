package com.convivium.module.billing.scheduler;

import com.convivium.module.billing.entity.PlatformInvoice;
import com.convivium.module.billing.repository.PlatformInvoiceRepository;
import com.convivium.module.condominium.entity.Condominium;
import com.convivium.module.condominium.repository.CondominiumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * Agendamento diario para gerar faturas e aplicar bloqueios por inadimplencia.
 *
 * Regras:
 * 1) Para cada condominio ACTIVE com plano, verifica se passaram 30 dias desde o ultimo pagamento
 *    (ou subscriptionStartedAt). Se sim, cria fatura PENDING.
 * 2) Faturas PENDING ha mais de 15 dias sao marcadas como OVERDUE.
 * 3) Faturas OVERDUE ha mais de 15 dias (total 30 dias sem pagar) → aplica blockType = PAYMENT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceScheduler {

    private static final DateTimeFormatter REF_MONTH = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int DAYS_OVERDUE_THRESHOLD = 15;
    private static final int DAYS_BLOCK_THRESHOLD = 15;

    private final CondominiumRepository condominiumRepository;
    private final PlatformInvoiceRepository platformInvoiceRepository;

    /**
     * Roda diariamente as 8h (horario do servidor).
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void processInvoices() {
        log.info("InvoiceScheduler: Iniciando processamento de faturas...");
        generatePendingInvoices();
        markOverdueInvoices();
        blockDelinquentCondominiums();
        log.info("InvoiceScheduler: Processamento finalizado.");
    }

    /**
     * Gera faturas PENDING para condominios ativos com plano que nao tenham fatura para o mes vigente.
     */
    void generatePendingInvoices() {
        List<Condominium> activeCondos = condominiumRepository.findByStatusOrderByName("ACTIVE");

        String currentMonth = YearMonth.now().format(REF_MONTH);

        for (Condominium condo : activeCondos) {
            if (condo.getPlan() == null) continue;

            // Verifica se ja existe fatura para o mes vigente
            Optional<PlatformInvoice> existing = platformInvoiceRepository
                    .findByCondominiumIdAndReferenceMonth(condo.getId(), currentMonth);
            if (existing.isPresent()) continue;

            // Verifica se passaram 30 dias desde a inscricao ou ultimo pagamento
            Instant reference = condo.getSubscriptionStartedAt();
            if (reference == null) {
                reference = condo.getCreatedAt();
            }
            if (reference == null) continue;

            long daysSinceReference = ChronoUnit.DAYS.between(reference, Instant.now());
            if (daysSinceReference < 30) continue;

            PlatformInvoice invoice = PlatformInvoice.builder()
                    .condominium(condo)
                    .plan(condo.getPlan())
                    .referenceMonth(currentMonth)
                    .amountCents(condo.getPlan().getPriceCents())
                    .status("PENDING")
                    .build();
            platformInvoiceRepository.save(invoice);
            log.info("InvoiceScheduler: Fatura criada para condo {} ref {}", condo.getId(), currentMonth);
        }
    }

    /**
     * Marca faturas PENDING ha mais de DAYS_OVERDUE_THRESHOLD dias como OVERDUE.
     */
    void markOverdueInvoices() {
        List<PlatformInvoice> pendingInvoices = platformInvoiceRepository.findByStatus("PENDING");

        Instant overdueThreshold = Instant.now().minus(DAYS_OVERDUE_THRESHOLD, ChronoUnit.DAYS);

        for (PlatformInvoice inv : pendingInvoices) {
            if (inv.getCreatedAt() != null && inv.getCreatedAt().isBefore(overdueThreshold)) {
                inv.setStatus("OVERDUE");
                platformInvoiceRepository.save(inv);
                log.info("InvoiceScheduler: Fatura {} marcada como OVERDUE", inv.getId());
            }
        }
    }

    /**
     * Aplica blockType = PAYMENT para condominios com faturas OVERDUE ha mais de DAYS_BLOCK_THRESHOLD.
     */
    void blockDelinquentCondominiums() {
        List<PlatformInvoice> overdueInvoices = platformInvoiceRepository.findByStatus("OVERDUE");

        Instant blockThreshold = Instant.now().minus(DAYS_BLOCK_THRESHOLD, ChronoUnit.DAYS);

        for (PlatformInvoice inv : overdueInvoices) {
            if (inv.getCreatedAt() != null && inv.getCreatedAt().isBefore(blockThreshold)) {
                Condominium condo = inv.getCondominium();
                if (condo.getBlockType() == null) {
                    condo.setBlockType("PAYMENT");
                    condo.setBlockedAt(Instant.now());
                    condo.setBlockedReason("Bloqueio automatico por inadimplencia");
                    condominiumRepository.save(condo);
                    log.info("InvoiceScheduler: Condominio {} bloqueado por inadimplencia", condo.getId());
                }
            }
        }
    }
}
