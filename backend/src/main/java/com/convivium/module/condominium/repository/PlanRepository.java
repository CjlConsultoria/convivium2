package com.convivium.module.condominium.repository;

import com.convivium.module.condominium.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findBySlug(String slug);

    List<Plan> findByActiveTrueOrderByName();
}
