package com.convivium.module.parcel.repository;

import com.convivium.module.parcel.entity.Parcel;
import com.convivium.module.parcel.entity.ParcelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelRepository extends JpaRepository<Parcel, Long> {

    Page<Parcel> findByCondominiumId(Long condominiumId, Pageable pageable);

    Page<Parcel> findByCondominiumIdAndStatus(Long condominiumId, ParcelStatus status, Pageable pageable);

    Page<Parcel> findByRecipientId(Long recipientId, Pageable pageable);

    Page<Parcel> findByRecipientIdAndStatus(Long recipientId, ParcelStatus status, Pageable pageable);

    Optional<Parcel> findByIdAndCondominiumId(Long id, Long condominiumId);

    long countByCondominiumIdAndStatusNot(Long condominiumId, ParcelStatus status);

    long countByRecipientIdAndStatusNot(Long recipientId, ParcelStatus status);

    List<Parcel> findByCondominiumIdAndUnit_IdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Long condominiumId, Long unitId, Instant since);

    List<Parcel> findByCondominiumIdAndRecipientIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Long condominiumId, Long recipientId, Instant since);
}
