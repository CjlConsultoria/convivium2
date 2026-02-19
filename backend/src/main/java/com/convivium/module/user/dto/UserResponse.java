package com.convivium.module.user.dto;

public record UserResponse(
        Long id,
        String uuid,
        String email,
        String name,
        String cpf,
        String phone,
        String photoUrl,
        boolean isActive,
        String role,
        String status,
        Long unitId,
        String unitIdentifier,
        String createdAt
) {
}
