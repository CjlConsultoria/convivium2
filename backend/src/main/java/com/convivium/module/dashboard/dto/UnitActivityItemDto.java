package com.convivium.module.dashboard.dto;

import java.time.Instant;

/**
 * Um item da linha do tempo de atividades da unidade (últimos 30 dias).
 * type: COMPLAINT, PARCEL
 * entityId e entityType permitem ao frontend montar o link (ex.: /complaints/123).
 */
public record UnitActivityItemDto(
        String type,
        Long entityId,
        String title,
        String description,
        Instant date,
        String badgeLabel
) {
}
