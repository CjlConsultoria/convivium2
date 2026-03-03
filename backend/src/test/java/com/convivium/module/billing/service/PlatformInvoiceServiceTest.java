package com.convivium.module.billing.service;

import com.convivium.module.billing.entity.PlatformInvoice;
import com.convivium.module.billing.repository.PlatformInvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformInvoiceServiceTest {

    @Mock
    private PlatformInvoiceRepository platformInvoiceRepository;

    @InjectMocks
    private PlatformInvoiceService platformInvoiceService;

    @Test
    void listByCondominium_returnsFormattedDtos() {
        PlatformInvoice inv = PlatformInvoice.builder()
                .id(1L)
                .referenceMonth("2024-03")
                .amountCents(9900)
                .status("PAID")
                .paidAt(Instant.parse("2024-03-15T10:00:00Z"))
                .createdAt(Instant.parse("2024-03-01T00:00:00Z"))
                .build();

        when(platformInvoiceRepository.findByCondominiumIdOrderByReferenceMonthDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(inv));

        List<PlatformInvoiceService.PlatformInvoiceDto> result = platformInvoiceService.listByCondominium(1L, 24);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).referenceMonth()).isEqualTo("2024-03");
        assertThat(result.get(0).referenceDisplay()).isEqualTo("Mar/2024");
        assertThat(result.get(0).amountCents()).isEqualTo(9900);
        assertThat(result.get(0).status()).isEqualTo("PAID");
    }

    @Test
    void listByCondominium_invalidReferenceMonth_fallsBack() {
        PlatformInvoice inv = PlatformInvoice.builder()
                .id(2L)
                .referenceMonth("invalid")
                .amountCents(5000)
                .status("PENDING")
                .build();

        when(platformInvoiceRepository.findByCondominiumIdOrderByReferenceMonthDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(inv));

        List<PlatformInvoiceService.PlatformInvoiceDto> result = platformInvoiceService.listByCondominium(1L, 12);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).referenceDisplay()).isEqualTo("invalid");
    }

    @Test
    void listByCondominium_emptyList() {
        when(platformInvoiceRepository.findByCondominiumIdOrderByReferenceMonthDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of());

        List<PlatformInvoiceService.PlatformInvoiceDto> result = platformInvoiceService.listByCondominium(1L, 24);

        assertThat(result).isEmpty();
    }

    @Test
    void listByCondominium_nullPaidAt_returnsNull() {
        PlatformInvoice inv = PlatformInvoice.builder()
                .id(3L)
                .referenceMonth("2024-12")
                .amountCents(15000)
                .status("PENDING")
                .paidAt(null)
                .createdAt(null)
                .build();

        when(platformInvoiceRepository.findByCondominiumIdOrderByReferenceMonthDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(inv));

        List<PlatformInvoiceService.PlatformInvoiceDto> result = platformInvoiceService.listByCondominium(1L, 24);

        assertThat(result.get(0).paidAt()).isNull();
        assertThat(result.get(0).createdAt()).isNull();
        assertThat(result.get(0).referenceDisplay()).isEqualTo("Dez/2024");
    }
}
