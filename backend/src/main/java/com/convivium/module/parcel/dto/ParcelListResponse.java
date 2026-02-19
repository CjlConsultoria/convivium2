package com.convivium.module.parcel.dto;

public record ParcelListResponse(
        Long id,
        String unitIdentifier,
        String recipientName,
        String receivedByName,
        String carrier,
        String trackingNumber,
        String description,
        String status,
        String deliveredAt,
        String createdAt
) {
}
