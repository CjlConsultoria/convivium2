package com.convivium.module.billing.repository;

import com.convivium.module.billing.entity.PlatformInvoice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformInvoiceRepository extends JpaRepository<PlatformInvoice, Long> {

    List<PlatformInvoice> findByCondominiumIdOrderByReferenceMonthDesc(Long condominiumId, Pageable pageable);

    Optional<PlatformInvoice> findByCondominiumIdAndReferenceMonth(Long condominiumId, String referenceMonth);

    Optional<PlatformInvoice> findByStripeSessionId(String stripeSessionId);
}
