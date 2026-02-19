package com.convivium.module.complaint.dto;

public record ComplaintResponseDto(
        Long id,
        String responderName,
        String responderRole,
        String message,
        boolean isInternal,
        String createdAt
) {
}
