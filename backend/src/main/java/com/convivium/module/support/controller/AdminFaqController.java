package com.convivium.module.support.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.support.dto.FaqCategoryResponse;
import com.convivium.module.support.dto.FaqItemCreateRequest;
import com.convivium.module.support.dto.FaqItemResponse;
import com.convivium.module.support.service.FaqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/faq")
@PreAuthorize("hasRole('PLATFORM_ADMIN')")
@RequiredArgsConstructor
public class AdminFaqController {

    private final FaqService faqService;

    // ---- Categories ----

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<FaqCategoryResponse>>> listCategories() {
        return ResponseEntity.ok(ApiResponse.ok(faqService.listAllCategories()));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<FaqCategoryResponse>> createCategory(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : 0;
        return ResponseEntity.ok(ApiResponse.ok(faqService.createCategory(name, sortOrder)));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<FaqCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : null;
        Boolean active = body.get("active") != null ? (Boolean) body.get("active") : null;
        return ResponseEntity.ok(ApiResponse.ok(faqService.updateCategory(id, name, sortOrder, active)));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        faqService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Categoria excluida"));
    }

    // ---- Items ----

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<FaqItemResponse>>> listItems() {
        return ResponseEntity.ok(ApiResponse.ok(faqService.listAllItems()));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<FaqItemResponse>> createItem(@Valid @RequestBody FaqItemCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(faqService.createItem(request)));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<FaqItemResponse>> updateItem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long categoryId = body.get("categoryId") != null ? ((Number) body.get("categoryId")).longValue() : null;
        String question = (String) body.get("question");
        String answer = (String) body.get("answer");
        Integer sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : null;
        Boolean published = body.get("published") != null ? (Boolean) body.get("published") : null;
        return ResponseEntity.ok(ApiResponse.ok(faqService.updateItem(id, categoryId, question, answer, sortOrder, published)));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        faqService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Item excluido"));
    }
}
