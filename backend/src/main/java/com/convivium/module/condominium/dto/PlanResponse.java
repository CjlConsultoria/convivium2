package com.convivium.module.condominium.dto;

public record PlanResponse(
        Long id,
        String name,
        String slug,
        Integer priceCents,
        String description,
        Integer maxUnits,
        Integer maxUsers,
        Boolean active
) {
}
