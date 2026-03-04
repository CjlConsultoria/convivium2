package com.convivium.module.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FaqItemCreateRequest(
        @NotNull Long categoryId,
        @NotBlank String question,
        @NotBlank String answer,
        Integer sortOrder
) {}
