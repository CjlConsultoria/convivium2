package com.convivium.module.support.repository;

import com.convivium.module.support.entity.FaqItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqItemRepository extends JpaRepository<FaqItem, Long> {

    List<FaqItem> findByCategoryIdAndPublishedTrueOrderBySortOrderAsc(Long categoryId);

    List<FaqItem> findByCategoryIdOrderBySortOrderAsc(Long categoryId);

    List<FaqItem> findByPublishedTrueOrderByCategorySortOrderAscSortOrderAsc();
}
