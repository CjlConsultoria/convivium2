package com.convivium.integration.google;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Verifica o ID token do Google (OAuth2) e retorna email, nome e foto.
 * Usado no login/cadastro do morador com Google.
 */
@Component
@Slf4j
public class GoogleIdTokenVerifier {

    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.google.client-id:}")
    private String clientId;

    /**
     * Valida o idToken e retorna os dados do usuário Google.
     * @throws InvalidGoogleTokenException se o token for inválido ou expirado
     */
    public GoogleUserInfo verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new InvalidGoogleTokenException("Token vazio");
        }
        try {
            String url = TOKEN_INFO_URL + idToken;
            String body = restTemplate.getForObject(url, String.class);
            if (body == null) {
                throw new InvalidGoogleTokenException("Resposta vazia do Google");
            }
            JsonNode node = objectMapper.readTree(body);
            if (node.has("error")) {
                throw new InvalidGoogleTokenException(node.path("error").asText());
            }
            if (clientId != null && !clientId.isBlank() && node.has("aud")) {
                String aud = node.path("aud").asText();
                if (!clientId.equals(aud)) {
                    throw new InvalidGoogleTokenException("Token não é para este app");
                }
            }
            String email = node.has("email") ? node.path("email").asText() : null;
            if (email == null || email.isBlank()) {
                throw new InvalidGoogleTokenException("Email não presente no token");
            }
            String name = node.has("name") ? node.path("name").asText() : email;
            String picture = node.has("picture") ? node.path("picture").asText(null) : null;
            return new GoogleUserInfo(email, name, picture);
        } catch (InvalidGoogleTokenException e) {
            throw e;
        } catch (Exception e) {
            log.debug("Falha ao verificar token Google", e);
            throw new InvalidGoogleTokenException("Token inválido ou expirado");
        }
    }

    public record GoogleUserInfo(String email, String name, String picture) {}

    public static class InvalidGoogleTokenException extends RuntimeException {
        public InvalidGoogleTokenException(String message) {
            super(message);
        }
    }
}
