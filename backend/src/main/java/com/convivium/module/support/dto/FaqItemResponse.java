package com.convivium.module.support.dto;

public record FaqItemResponse(
        Long id,
        Long categoryId,
        String categoryName,
        String question,
        String answer,
        Integer sortOrder,
        Boolean published
) {}
