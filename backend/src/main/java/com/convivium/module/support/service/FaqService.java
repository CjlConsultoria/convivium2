package com.convivium.module.support.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.module.support.dto.FaqCategoryResponse;
import com.convivium.module.support.dto.FaqItemCreateRequest;
import com.convivium.module.support.dto.FaqItemResponse;
import com.convivium.module.support.entity.FaqCategory;
import com.convivium.module.support.entity.FaqItem;
import com.convivium.module.support.repository.FaqCategoryRepository;
import com.convivium.module.support.repository.FaqItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqCategoryRepository faqCategoryRepository;
    private final FaqItemRepository faqItemRepository;

    /**
     * Lista todas as categorias ativas com itens publicados (publico).
     */
    @Transactional(readOnly = true)
    public List<FaqCategoryResponse> listPublicFaq() {
        List<FaqCategory> categories = faqCategoryRepository.findByActiveTrueOrderBySortOrderAsc();
        List<FaqItem> publishedItems = faqItemRepository.findByPublishedTrueOrderByCategorySortOrderAscSortOrderAsc();

        Map<Long, List<FaqItemResponse>> itemsByCategory = publishedItems.stream()
                .filter(item -> item.getCategory() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getCategory().getId(),
                        Collectors.mapping(this::toItemResponse, Collectors.toList())
                ));

        return categories.stream()
                .map(cat -> new FaqCategoryResponse(
                        cat.getId(),
                        cat.getName(),
                        cat.getSortOrder(),
                        cat.getActive(),
                        itemsByCategory.getOrDefault(cat.getId(), List.of())
                ))
                .filter(cat -> !cat.items().isEmpty())
                .toList();
    }

    // ---- Admin CRUD ----

    @Transactional(readOnly = true)
    public List<FaqCategoryResponse> listAllCategories() {
        return faqCategoryRepository.findAllByOrderBySortOrderAsc().stream()
                .map(cat -> new FaqCategoryResponse(cat.getId(), cat.getName(), cat.getSortOrder(), cat.getActive(), List.of()))
                .toList();
    }

    @Transactional
    public FaqCategoryResponse createCategory(String name, Integer sortOrder) {
        FaqCategory cat = FaqCategory.builder()
                .name(name)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .build();
        cat = faqCategoryRepository.save(cat);
        return new FaqCategoryResponse(cat.getId(), cat.getName(), cat.getSortOrder(), cat.getActive(), List.of());
    }

    @Transactional
    public FaqCategoryResponse updateCategory(Long id, String name, Integer sortOrder, Boolean active) {
        FaqCategory cat = faqCategoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria nao encontrada", "FAQ_CATEGORY_NOT_FOUND"));
        if (name != null) cat.setName(name);
        if (sortOrder != null) cat.setSortOrder(sortOrder);
        if (active != null) cat.setActive(active);
        cat = faqCategoryRepository.save(cat);
        return new FaqCategoryResponse(cat.getId(), cat.getName(), cat.getSortOrder(), cat.getActive(), List.of());
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!faqCategoryRepository.existsById(id)) {
            throw new BusinessException("Categoria nao encontrada", "FAQ_CATEGORY_NOT_FOUND");
        }
        faqCategoryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<FaqItemResponse> listAllItems() {
        return faqItemRepository.findAll().stream()
                .map(this::toItemResponse)
                .toList();
    }

    @Transactional
    public FaqItemResponse createItem(FaqItemCreateRequest request) {
        FaqCategory category = faqCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException("Categoria nao encontrada", "FAQ_CATEGORY_NOT_FOUND"));

        FaqItem item = FaqItem.builder()
                .category(category)
                .question(request.question())
                .answer(request.answer())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .build();
        item = faqItemRepository.save(item);
        return toItemResponse(item);
    }

    @Transactional
    public FaqItemResponse updateItem(Long id, Long categoryId, String question, String answer, Integer sortOrder, Boolean published) {
        FaqItem item = faqItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Item nao encontrado", "FAQ_ITEM_NOT_FOUND"));

        if (categoryId != null) {
            FaqCategory cat = faqCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException("Categoria nao encontrada", "FAQ_CATEGORY_NOT_FOUND"));
            item.setCategory(cat);
        }
        if (question != null) item.setQuestion(question);
        if (answer != null) item.setAnswer(answer);
        if (sortOrder != null) item.setSortOrder(sortOrder);
        if (published != null) item.setPublished(published);
        item = faqItemRepository.save(item);
        return toItemResponse(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        if (!faqItemRepository.existsById(id)) {
            throw new BusinessException("Item nao encontrado", "FAQ_ITEM_NOT_FOUND");
        }
        faqItemRepository.deleteById(id);
    }

    private FaqItemResponse toItemResponse(FaqItem item) {
        return new FaqItemResponse(
                item.getId(),
                item.getCategory() != null ? item.getCategory().getId() : null,
                item.getCategory() != null ? item.getCategory().getName() : null,
                item.getQuestion(),
                item.getAnswer(),
                item.getSortOrder(),
                item.getPublished()
        );
    }
}
