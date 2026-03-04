package com.convivium.module.support.dto;

import java.util.List;

public record FaqCategoryResponse(
        Long id,
        String name,
        Integer sortOrder,
        Boolean active,
        List<FaqItemResponse> items
) {}
