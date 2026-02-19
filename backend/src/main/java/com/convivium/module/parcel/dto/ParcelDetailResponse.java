package com.convivium.module.parcel.dto;

import java.util.List;

public record ParcelDetailResponse(
        Long id,
        String unitIdentifier,
        String recipientName,
        String receivedByName,
        String carrier,
        String trackingNumber,
        String description,
        String status,
        String deliveredAt,
        String createdAt,
        String pickupCode,
        String residentCode,
        List<ParcelPhotoDto> photos
) {
}
