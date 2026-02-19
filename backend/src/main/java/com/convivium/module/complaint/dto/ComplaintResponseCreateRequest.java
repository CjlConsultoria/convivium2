package com.convivium.module.complaint.dto;

import jakarta.validation.constraints.NotBlank;

public record ComplaintResponseCreateRequest(
        @NotBlank(message = "Mensagem e obrigatoria")
        String message,

        boolean isInternal
) {
}
