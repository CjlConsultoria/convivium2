package com.convivium.module.condominium.repository;

import com.convivium.module.condominium.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    List<Unit> findByCondominiumId(Long condominiumId);

    List<Unit> findByCondominiumIdAndBuilding_Id(Long condominiumId, Long buildingId);

    Optional<Unit> findByCondominiumIdAndId(Long condominiumId, Long id);

    boolean existsByCondominiumIdAndBuilding_IdAndIdentifier(Long condominiumId, Long buildingId, String identifier);
}
