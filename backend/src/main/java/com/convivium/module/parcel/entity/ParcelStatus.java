package com.convivium.module.parcel.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ParcelStatus {

    RECEIVED("Recebida"),
    NOTIFIED("Notificado"),
    PICKUP_REQUESTED("Retirada Solicitada"),
    VERIFIED("Verificada"),
    DELIVERED("Entregue");

    private final String displayName;
}
