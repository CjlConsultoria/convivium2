package com.convivium.module.auth.dto;

/**
 * Resposta do login/cadastro com Google.
 * Se needsRegistration = true, o front deve exibir o formulário para completar (condomínio, unidade, etc.)
 * e depois chamar POST /auth/register-google.
 */
public record GoogleAuthResponse(
        boolean needsRegistration,
        String email,
        String name,
        String picture,
        String accessToken,
        String refreshToken,
        Long expiresIn,
        UserInfoResponse user
) {
    public static GoogleAuthResponse needsRegistration(String email, String name, String picture) {
        return new GoogleAuthResponse(true, email, name, picture, null, null, null, null);
    }

    public static GoogleAuthResponse loggedIn(LoginResponse loginResponse) {
        return new GoogleAuthResponse(
                false,
                null,
                null,
                null,
                loginResponse.accessToken(),
                loginResponse.refreshToken(),
                loginResponse.expiresIn(),
                loginResponse.user()
        );
    }
}
