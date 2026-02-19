package com.convivium.module.condominium.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Estrutura revisada pelo admin para ser salva no banco (blocos e unidades).
 */
public record ApplyStructureRequest(
        @NotNull @Valid
        List<StructurePreviewResponse.BuildingPreviewItem> buildings,

        @NotNull @Valid
        List<StructurePreviewResponse.UnitPreviewItem> units
) {
}
