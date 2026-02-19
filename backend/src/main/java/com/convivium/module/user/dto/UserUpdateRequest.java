package com.convivium.module.user.dto;

public record UserUpdateRequest(
        String name,
        String phone,
        String photoUrl,
        Boolean isActive,
        Long unitId
) {
}
