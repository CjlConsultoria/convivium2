package com.convivium.module.condominium.repository;

import com.convivium.module.condominium.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    List<Building> findByCondominiumId(Long condominiumId);

    Optional<Building> findByCondominiumIdAndName(Long condominiumId, String name);
}
