package com.convivium.module.auth.dto;

import jakarta.validation.constraints.Size;

/**
 * Dados que o próprio usuário pode alterar no perfil (nome, telefone).
 * E-mail e CPF não são editáveis por esta rota.
 */
public record UpdateMyProfileRequest(
        @Size(max = 255) String name,
        @Size(max = 50) String phone
) {
}
