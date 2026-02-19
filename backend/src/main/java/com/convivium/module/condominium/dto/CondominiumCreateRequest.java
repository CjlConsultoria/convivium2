package com.convivium.module.condominium.dto;

import jakarta.validation.constraints.NotBlank;

public record CondominiumCreateRequest(
        @NotBlank(message = "Nome e obrigatorio")
        String name,

        String cnpj,

        String email,

        String phone,

        String addressStreet,

        String addressNumber,

        String addressComplement,

        String addressNeighborhood,

        String addressCity,

        String addressState,

        String addressZip
) {
}
