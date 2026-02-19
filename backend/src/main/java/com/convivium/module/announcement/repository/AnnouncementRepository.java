package com.convivium.module.announcement.repository;

import com.convivium.module.announcement.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Page<Announcement> findByCondominiumIdOrderByCreatedAtDesc(Long condominiumId, Pageable pageable);

    Optional<Announcement> findByIdAndCondominiumId(Long id, Long condominiumId);
}
