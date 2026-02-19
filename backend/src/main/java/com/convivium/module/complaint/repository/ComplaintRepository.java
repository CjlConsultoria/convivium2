package com.convivium.module.complaint.repository;

import com.convivium.module.complaint.entity.Complaint;
import com.convivium.module.complaint.entity.ComplaintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Page<Complaint> findByCondominiumId(Long condominiumId, Pageable pageable);

    Page<Complaint> findByCondominiumIdAndStatus(Long condominiumId, ComplaintStatus status, Pageable pageable);

    Page<Complaint> findByCondominiumIdAndComplainantId(Long condominiumId, Long complainantId, Pageable pageable);

    Page<Complaint> findByCondominiumIdAndComplainantIdAndStatus(Long condominiumId, Long complainantId, ComplaintStatus status, Pageable pageable);

    Optional<Complaint> findByIdAndCondominiumId(Long id, Long condominiumId);

    long countByCondominiumIdAndStatus(Long condominiumId, ComplaintStatus status);

    long countByCondominiumIdAndUnit_IdAndStatus(Long condominiumId, Long unitId, ComplaintStatus status);

    long countByCondominiumIdAndComplainantIdAndStatus(Long condominiumId, Long complainantId, ComplaintStatus status);

    List<Complaint> findByCondominiumIdAndUnit_IdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Long condominiumId, Long unitId, Instant since);

    List<Complaint> findByCondominiumIdAndComplainantIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Long condominiumId, Long complainantId, Instant since);
}
