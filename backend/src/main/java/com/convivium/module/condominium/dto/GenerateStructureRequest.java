package com.convivium.module.condominium.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * Parâmetros para gerar em lote blocos e unidades de um condomínio.
 * identifierFormat: "1" = 1,2,3... | "01" = 01,02... | "101" = andar+numero (ex: 101,102).
 * identifierStart: numero em que a sequencia comeca (ex: 11 → 11,12,13... ou 111,112 no formato 101).
 */
public record GenerateStructureRequest(
        @Min(value = 1, message = "Deve haver pelo menos 1 bloco")
        @Max(value = 26, message = "No maximo 26 blocos (A a Z)")
        int blocksCount,

        @Min(value = 1, message = "Deve haver pelo menos 1 apartamento por andar")
        @Max(value = 50, message = "No maximo 50 apartamentos por andar")
        int unitsPerFloor,

        @Min(value = 1, message = "Deve haver pelo menos 1 andar")
        @Max(value = 100, message = "No maximo 100 andares por bloco")
        int floorsPerBlock,

        @Pattern(regexp = "^$|1|01|101", message = "Formato deve ser 1, 01 ou 101")
        String identifierFormat,

        @Min(value = 0, message = "Inicio deve ser >= 0")
        @Max(value = 999, message = "Inicio deve ser <= 999")
        Integer identifierStart
) {
    /** Formato efetivo: 1, 01 ou 101 (default). */
    public String identifierFormat() {
        return identifierFormat != null && !identifierFormat.isBlank() ? identifierFormat : "101";
    }
}
