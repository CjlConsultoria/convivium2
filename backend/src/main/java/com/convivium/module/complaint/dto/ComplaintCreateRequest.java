package com.convivium.module.complaint.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComplaintCreateRequest(
        @NotBlank(message = "Categoria e obrigatoria")
        String category,

        @NotBlank(message = "Titulo e obrigatorio")
        @Size(max = 200, message = "Titulo deve ter no maximo 200 caracteres")
        String title,

        @NotBlank(message = "Descricao e obrigatoria")
        String description,

        boolean isAnonymous,

        Long unitId,

        String priority
) {
}
