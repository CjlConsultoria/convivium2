package com.convivium.module.auth.dto;

/**
 * Resposta do cadastro com Google.
 * Se needsApproval = true, o morador ficou pendente de aprovacao do sindico/admin (sem login).
 * Se login != null, o usuario foi logado (ja aprovado ou ja tinha vínculo ativo).
 */
public record RegisterGoogleResponse(
        boolean needsApproval,
        String message,
        LoginResponse login
) {
    public static RegisterGoogleResponse pendingApproval(String message) {
        return new RegisterGoogleResponse(true, message, null);
    }

    public static RegisterGoogleResponse loggedIn(LoginResponse login) {
        return new RegisterGoogleResponse(false, null, login);
    }
}
