package com.convivium.module.complaint.dto;

public record ComplaintListResponse(
        Long id,
        String complainantName,
        boolean isAnonymous,
        String category,
        String title,
        String status,
        String priority,
        String unitIdentifier,
        String createdAt,
        int responseCount
) {
}
