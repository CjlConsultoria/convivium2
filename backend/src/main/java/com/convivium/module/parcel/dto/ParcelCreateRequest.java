package com.convivium.module.parcel.dto;

import jakarta.validation.constraints.NotNull;

public record ParcelCreateRequest(
        @NotNull(message = "Unidade e obrigatoria")
        Long unitId,

        Long recipientId,

        String carrier,

        String trackingNumber,

        String description
) {
}
