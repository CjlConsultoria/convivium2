package com.convivium.module.condominium.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Resposta do preview: lista de blocos e unidades que seriam criados (sem salvar no banco).
 */
public record StructurePreviewResponse(
        List<BuildingPreviewItem> buildings,
        List<UnitPreviewItem> units
) {
    public record BuildingPreviewItem(
            @NotBlank @Size(max = 50) String name,
            @NotNull Integer floors
    ) {}

    public record UnitPreviewItem(
            @NotBlank @Size(max = 50) String buildingName,
            @NotNull Integer floor,
            @NotBlank @Size(max = 20) String identifier
    ) {}
}
