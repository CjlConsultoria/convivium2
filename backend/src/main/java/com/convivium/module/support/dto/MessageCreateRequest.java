package com.convivium.module.support.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageCreateRequest(
        @NotBlank String message
) {}
