package com.convivium.module.complaint.dto;

import jakarta.validation.constraints.NotBlank;

public record ComplaintStatusUpdateRequest(
        @NotBlank(message = "Status e obrigatorio")
        String status
) {
}
