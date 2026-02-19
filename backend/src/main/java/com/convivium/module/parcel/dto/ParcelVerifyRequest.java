package com.convivium.module.parcel.dto;

import jakarta.validation.constraints.NotBlank;

/** Código informado pelo morador na hora da retirada; o porteiro digita no sistema para validar. Só existe um código, conhecido apenas pelo morador. */
public record ParcelVerifyRequest(
        @NotBlank(message = "Informe o codigo que o morador passou para voce")
        String code,

        String verificationMethod
) {
    public ParcelVerifyRequest {
        if (verificationMethod == null || verificationMethod.isBlank()) {
            verificationMethod = "CODE_MATCH";
        }
    }
}
