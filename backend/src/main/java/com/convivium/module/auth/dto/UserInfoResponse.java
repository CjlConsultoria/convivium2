package com.convivium.module.auth.dto;

import java.util.List;

public record UserInfoResponse(
        Long id,
        String uuid,
        String email,
        String name,
        String phone,
        String photoUrl,
        boolean isPlatformAdmin,
        List<CondominiumRoleResponse> condominiumRoles
) {
}
