package com.convivium.module.condominium.dto;

import java.math.BigDecimal;
import java.util.List;

public record UnitResponse(
        Long id,
        Long buildingId,
        String buildingName,
        String identifier,
        Integer floor,
        String type,
        BigDecimal areaSqm,
        boolean isOccupied,
        List<UnitResidentInfo> residents
) {
}
