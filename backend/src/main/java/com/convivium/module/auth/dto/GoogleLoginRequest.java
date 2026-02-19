package com.convivium.module.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "Token do Google e obrigatorio")
        String idToken
) {
}
