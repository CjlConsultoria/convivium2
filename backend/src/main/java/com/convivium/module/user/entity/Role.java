package com.convivium.module.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    SINDICO("Sindico"),
    SUB_SINDICO("Subsindico"),
    CONSELHEIRO("Conselheiro"),
    PORTEIRO("Porteiro"),
    ZELADOR("Zelador"),
    FAXINEIRA("Faxineira"),
    MORADOR("Morador");

    private final String displayName;
}
