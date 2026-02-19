package com.convivium.module.billing.service;

import com.convivium.module.billing.entity.PlatformInvoice;
import com.convivium.module.billing.repository.PlatformInvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformInvoiceService {

    private static final String[] MONTH_NAMES = { "Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez" };

    private final PlatformInvoiceRepository platformInvoiceRepository;

    @Transactional(readOnly = true)
    public List<PlatformInvoiceDto> listByCondominium(Long condominiumId, int limit) {
        return platformInvoiceRepository
                .findByCondominiumIdOrderByReferenceMonthDesc(condominiumId, PageRequest.of(0, limit))
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private PlatformInvoiceDto toDto(PlatformInvoice inv) {
        String referenceDisplay = inv.getReferenceMonth();
        try {
            YearMonth ym = YearMonth.parse(inv.getReferenceMonth());
            referenceDisplay = MONTH_NAMES[ym.getMonthValue() - 1] + "/" + ym.getYear();
        } catch (Exception ignored) { }
        return new PlatformInvoiceDto(
                inv.getId(),
                inv.getReferenceMonth(),
                referenceDisplay,
                inv.getAmountCents(),
                inv.getStatus(),
                inv.getPaidAt() != null ? inv.getPaidAt().toString() : null,
                inv.getCreatedAt() != null ? inv.getCreatedAt().toString() : null
        );
    }

    public record PlatformInvoiceDto(
            Long id,
            String referenceMonth,
            String referenceDisplay,
            Integer amountCents,
            String status,
            String paidAt,
            String createdAt
    ) {}
}
