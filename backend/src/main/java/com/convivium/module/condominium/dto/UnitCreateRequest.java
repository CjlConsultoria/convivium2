package com.convivium.module.condominium.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record UnitCreateRequest(
        Long buildingId,

        @NotBlank(message = "Identificador e obrigatorio")
        String identifier,

        Integer floor,

        String type,

        BigDecimal areaSqm
) {
}
