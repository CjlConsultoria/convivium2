package com.convivium.module.user.repository;

import com.convivium.module.user.entity.Role;
import com.convivium.module.user.entity.UserCondominiumRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCondominiumRoleRepository extends JpaRepository<UserCondominiumRole, Long> {

    @Query("SELECT ucr FROM UserCondominiumRole ucr JOIN FETCH ucr.user JOIN FETCH ucr.unit WHERE ucr.condominium.id = :condoId AND ucr.unit IS NOT NULL AND ucr.status = :status")
    List<UserCondominiumRole> findByCondominiumIdAndUnitNotNullAndStatus(@Param("condoId") Long condoId, @Param("status") String status);

    List<UserCondominiumRole> findAllByUserIdAndCondominiumId(Long userId, Long condominiumId);

    List<UserCondominiumRole> findByCondominiumIdAndStatus(Long condominiumId, String status);

    List<UserCondominiumRole> findByCondominiumId(Long condominiumId);

    Page<UserCondominiumRole> findByCondominiumId(Long condominiumId, Pageable pageable);

    Optional<UserCondominiumRole> findByUserIdAndCondominiumIdAndRole(Long userId, Long condominiumId, Role role);

    Optional<UserCondominiumRole> findByUserIdAndCondominiumId(Long userId, Long condominiumId);

    boolean existsByUserIdAndCondominiumId(Long userId, Long condominiumId);

    long countByCondominiumId(Long condominiumId);

    boolean existsByUnit_Id(Long unitId);
}
