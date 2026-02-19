package com.convivium.module.parcel.repository;

import com.convivium.module.parcel.entity.ParcelVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParcelVerificationRepository extends JpaRepository<ParcelVerification, Long> {

    Optional<ParcelVerification> findByParcelId(Long parcelId);
}
