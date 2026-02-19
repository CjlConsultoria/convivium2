package com.convivium.module.condominium.repository;

import com.convivium.module.condominium.entity.Condominium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CondominiumRepository extends JpaRepository<Condominium, Long> {

    Optional<Condominium> findBySlug(String slug);

    List<Condominium> findByStatusOrderByName(String status);

    boolean existsBySlug(String slug);

    boolean existsByCnpj(String cnpj);
}
