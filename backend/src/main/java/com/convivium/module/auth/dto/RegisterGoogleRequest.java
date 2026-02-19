package com.convivium.module.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterGoogleRequest(
        @NotBlank(message = "Token do Google e obrigatorio")
        String idToken,

        @NotNull(message = "Condominio e obrigatorio")
        Long condominiumId,

        @NotNull(message = "Unidade e obrigatoria")
        Long unitId,

        String phone
) {
}
