package com.convivium.module.complaint.dto;

import java.util.List;

public record ComplaintDetailResponse(
        Long id,
        String complainantName,
        boolean isAnonymous,
        String category,
        String title,
        String description,
        String status,
        String priority,
        String unitIdentifier,
        String createdAt,
        String updatedAt,
        List<ComplaintResponseDto> responses,
        List<ComplaintAttachmentDto> attachments
) {
}
