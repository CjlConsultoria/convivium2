package com.convivium.module.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token e obrigatorio")
        String refreshToken
) {
}
