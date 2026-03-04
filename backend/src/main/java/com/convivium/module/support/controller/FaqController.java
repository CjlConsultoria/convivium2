package com.convivium.module.support.controller;

import com.convivium.common.dto.ApiResponse;
import com.convivium.module.support.dto.FaqCategoryResponse;
import com.convivium.module.support.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    /**
     * Lista categorias com itens publicados (acesso publico autenticado).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FaqCategoryResponse>>> listFaq() {
        List<FaqCategoryResponse> result = faqService.listPublicFaq();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
