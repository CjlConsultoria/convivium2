package com.convivium.module.support.repository;

import com.convivium.module.support.entity.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqCategoryRepository extends JpaRepository<FaqCategory, Long> {

    List<FaqCategory> findByActiveTrueOrderBySortOrderAsc();

    List<FaqCategory> findAllByOrderBySortOrderAsc();
}
