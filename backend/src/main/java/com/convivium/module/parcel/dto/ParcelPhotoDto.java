package com.convivium.module.parcel.dto;

public record ParcelPhotoDto(
        Long id,
        String photoUrl,
        String photoType,
        String createdAt
) {
}
