package com.convivium.module.support.service;

import com.convivium.common.exception.BusinessException;
import com.convivium.module.support.dto.FaqCategoryResponse;
import com.convivium.module.support.dto.FaqItemCreateRequest;
import com.convivium.module.support.dto.FaqItemResponse;
import com.convivium.module.support.entity.FaqCategory;
import com.convivium.module.support.entity.FaqItem;
import com.convivium.module.support.repository.FaqCategoryRepository;
import com.convivium.module.support.repository.FaqItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FaqServiceTest {

    @Mock
    private FaqCategoryRepository faqCategoryRepository;

    @Mock
    private FaqItemRepository faqItemRepository;

    @InjectMocks
    private FaqService faqService;

    // ---- listPublicFaq ----

    @Test
    void listPublicFaq_returnsCategoriesWithPublishedItems() {
        FaqCategory cat = FaqCategory.builder().id(1L).name("Geral").sortOrder(0).active(true).build();
        FaqItem item = FaqItem.builder().id(1L).category(cat).question("Q1?").answer("A1").sortOrder(0).published(true).build();

        when(faqCategoryRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(cat));
        when(faqItemRepository.findByPublishedTrueOrderByCategorySortOrderAscSortOrderAsc()).thenReturn(List.of(item));

        List<FaqCategoryResponse> result = faqService.listPublicFaq();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Geral");
        assertThat(result.get(0).items()).hasSize(1);
        assertThat(result.get(0).items().get(0).question()).isEqualTo("Q1?");
    }

    @Test
    void listPublicFaq_excludesCategoriesWithoutItems() {
        FaqCategory cat = FaqCategory.builder().id(1L).name("Vazia").sortOrder(0).active(true).build();

        when(faqCategoryRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(cat));
        when(faqItemRepository.findByPublishedTrueOrderByCategorySortOrderAscSortOrderAsc()).thenReturn(List.of());

        List<FaqCategoryResponse> result = faqService.listPublicFaq();

        assertThat(result).isEmpty();
    }

    // ---- Category CRUD ----

    @Test
    void listAllCategories_returnsAll() {
        FaqCategory cat1 = FaqCategory.builder().id(1L).name("Cat1").sortOrder(0).active(true).build();
        FaqCategory cat2 = FaqCategory.builder().id(2L).name("Cat2").sortOrder(1).active(false).build();

        when(faqCategoryRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(cat1, cat2));

        List<FaqCategoryResponse> result = faqService.listAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("Cat1");
        assertThat(result.get(1).active()).isFalse();
    }

    @Test
    void createCategory_savesAndReturns() {
        FaqCategory saved = FaqCategory.builder().id(1L).name("Nova").sortOrder(5).active(true).build();
        when(faqCategoryRepository.save(any())).thenReturn(saved);

        FaqCategoryResponse result = faqService.createCategory("Nova", 5);

        assertThat(result.name()).isEqualTo("Nova");
        assertThat(result.sortOrder()).isEqualTo(5);
        verify(faqCategoryRepository).save(any(FaqCategory.class));
    }

    @Test
    void createCategory_usesDefaultSortOrder() {
        FaqCategory saved = FaqCategory.builder().id(1L).name("Nova").sortOrder(0).active(true).build();
        when(faqCategoryRepository.save(any())).thenReturn(saved);

        FaqCategoryResponse result = faqService.createCategory("Nova", null);

        assertThat(result.sortOrder()).isEqualTo(0);
    }

    @Test
    void updateCategory_updatesFields() {
        FaqCategory existing = FaqCategory.builder().id(1L).name("Old").sortOrder(0).active(true).build();
        when(faqCategoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(faqCategoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FaqCategoryResponse result = faqService.updateCategory(1L, "New", 10, false);

        assertThat(result.name()).isEqualTo("New");
        assertThat(result.sortOrder()).isEqualTo(10);
        assertThat(result.active()).isFalse();
    }

    @Test
    void updateCategory_notFound_throws() {
        when(faqCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> faqService.updateCategory(99L, "X", null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Categoria nao encontrada");
    }

    @Test
    void deleteCategory_deletesExisting() {
        when(faqCategoryRepository.existsById(1L)).thenReturn(true);

        faqService.deleteCategory(1L);

        verify(faqCategoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_notFound_throws() {
        when(faqCategoryRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> faqService.deleteCategory(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Categoria nao encontrada");
    }

    // ---- Item CRUD ----

    @Test
    void listAllItems_returnsAll() {
        FaqCategory cat = FaqCategory.builder().id(1L).name("Cat").build();
        FaqItem item = FaqItem.builder().id(1L).category(cat).question("Q?").answer("A").sortOrder(0).published(true).build();

        when(faqItemRepository.findAll()).thenReturn(List.of(item));

        List<FaqItemResponse> result = faqService.listAllItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).question()).isEqualTo("Q?");
        assertThat(result.get(0).categoryName()).isEqualTo("Cat");
    }

    @Test
    void createItem_savesWithCategory() {
        FaqCategory cat = FaqCategory.builder().id(1L).name("Cat").build();
        FaqItemCreateRequest request = new FaqItemCreateRequest(1L, "Q?", "A", 5);

        when(faqCategoryRepository.findById(1L)).thenReturn(Optional.of(cat));
        FaqItem saved = FaqItem.builder().id(1L).category(cat).question("Q?").answer("A").sortOrder(5).published(true).build();
        when(faqItemRepository.save(any())).thenReturn(saved);

        FaqItemResponse result = faqService.createItem(request);

        assertThat(result.question()).isEqualTo("Q?");
        assertThat(result.categoryId()).isEqualTo(1L);
    }

    @Test
    void createItem_categoryNotFound_throws() {
        FaqItemCreateRequest request = new FaqItemCreateRequest(99L, "Q?", "A", null);
        when(faqCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> faqService.createItem(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Categoria nao encontrada");
    }

    @Test
    void updateItem_updatesFields() {
        FaqCategory cat = FaqCategory.builder().id(1L).name("Cat").build();
        FaqItem existing = FaqItem.builder().id(1L).category(cat).question("Old").answer("OldA").sortOrder(0).published(true).build();

        when(faqItemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(faqItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FaqItemResponse result = faqService.updateItem(1L, null, "New", "NewA", 10, false);

        assertThat(result.question()).isEqualTo("New");
        assertThat(result.answer()).isEqualTo("NewA");
        assertThat(result.published()).isFalse();
    }

    @Test
    void updateItem_changeCategory() {
        FaqCategory oldCat = FaqCategory.builder().id(1L).name("Old").build();
        FaqCategory newCat = FaqCategory.builder().id(2L).name("New").build();
        FaqItem existing = FaqItem.builder().id(1L).category(oldCat).question("Q").answer("A").sortOrder(0).published(true).build();

        when(faqItemRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(faqCategoryRepository.findById(2L)).thenReturn(Optional.of(newCat));
        when(faqItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FaqItemResponse result = faqService.updateItem(1L, 2L, null, null, null, null);

        assertThat(result.categoryId()).isEqualTo(2L);
    }

    @Test
    void updateItem_notFound_throws() {
        when(faqItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> faqService.updateItem(99L, null, null, null, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Item nao encontrado");
    }

    @Test
    void deleteItem_deletesExisting() {
        when(faqItemRepository.existsById(1L)).thenReturn(true);

        faqService.deleteItem(1L);

        verify(faqItemRepository).deleteById(1L);
    }

    @Test
    void deleteItem_notFound_throws() {
        when(faqItemRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> faqService.deleteItem(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Item nao encontrado");
    }
}
