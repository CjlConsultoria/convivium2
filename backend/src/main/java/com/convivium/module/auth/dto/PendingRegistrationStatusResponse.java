package com.convivium.module.auth.dto;

/**
 * Resposta ao verificar se o e-mail já possui cadastro pendente de aprovação.
 */
public record PendingRegistrationStatusResponse(
        boolean hasPendingApproval,
        String message,
        String condominiumName
) {
    public static PendingRegistrationStatusResponse noPending() {
        return new PendingRegistrationStatusResponse(false, null, null);
    }

    public static PendingRegistrationStatusResponse pending(String message, String condominiumName) {
        return new PendingRegistrationStatusResponse(true, message, condominiumName);
    }
}
