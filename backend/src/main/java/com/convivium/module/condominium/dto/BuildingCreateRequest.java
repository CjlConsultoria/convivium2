package com.convivium.module.condominium.dto;

import jakarta.validation.constraints.NotBlank;

public record BuildingCreateRequest(
        @NotBlank(message = "Nome e obrigatorio")
        String name,

        Integer floors
) {
}
