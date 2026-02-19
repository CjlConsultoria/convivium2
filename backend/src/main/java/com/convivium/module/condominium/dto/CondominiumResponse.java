package com.convivium.module.condominium.dto;

public record CondominiumResponse(
        Long id,
        String name,
        String slug,
        String cnpj,
        String email,
        String phone,
        String addressStreet,
        String addressNumber,
        String addressComplement,
        String addressNeighborhood,
        String addressCity,
        String addressState,
        String addressZip,
        String logoUrl,
        Long planId,
        String planName,
        Integer planPriceCents,
        String status,
        String createdAt
) {
}
