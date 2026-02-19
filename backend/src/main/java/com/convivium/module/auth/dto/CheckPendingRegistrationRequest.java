package com.convivium.module.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckPendingRegistrationRequest(
        @NotBlank(message = "Token do Google e obrigatorio")
        String idToken
) {
}
