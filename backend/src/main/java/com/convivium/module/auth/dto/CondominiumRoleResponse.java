package com.convivium.module.auth.dto;

public record CondominiumRoleResponse(
        Long condominiumId,
        String condominiumName,
        String role,
        String status,
        Long unitId,
        String unitIdentifier
) {
}
