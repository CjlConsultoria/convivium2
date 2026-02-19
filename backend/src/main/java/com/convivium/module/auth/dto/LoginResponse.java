package com.convivium.module.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserInfoResponse user
) {
}
