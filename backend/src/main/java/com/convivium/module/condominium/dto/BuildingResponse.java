package com.convivium.module.condominium.dto;

public record BuildingResponse(
        Long id,
        String name,
        Integer floors,
        String createdAt
) {
}
