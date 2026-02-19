package com.convivium.module.complaint.repository;

import com.convivium.module.complaint.entity.ComplaintResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintResponseRepository extends JpaRepository<ComplaintResponse, Long> {

    List<ComplaintResponse> findByComplaintIdOrderByCreatedAtAsc(Long complaintId);
}
