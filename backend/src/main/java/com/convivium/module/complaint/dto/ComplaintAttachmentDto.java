package com.convivium.module.complaint.dto;

public record ComplaintAttachmentDto(
        Long id,
        String fileName,
        String fileUrl,
        String fileType,
        String createdAt
) {
}
