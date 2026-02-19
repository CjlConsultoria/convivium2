package com.convivium.module.complaint.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplaintCategory {

    NOISE("Barulho"),
    MAINTENANCE("Manutencao"),
    SECURITY("Seguranca"),
    PARKING("Estacionamento"),
    PET("Animais de Estimacao"),
    COMMON_AREA("Area Comum"),
    OTHER("Outros");

    private final String displayName;
}
