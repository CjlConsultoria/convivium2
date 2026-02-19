package com.convivium.integration.whatsapp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Envio de mensagens via WhatsApp Business API (Meta / Facebook).
 * Usa templates aprovados no Meta Business Manager para iniciar conversas.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "app.whatsapp.enabled", havingValue = "true")
public class MetaWhatsAppService implements WhatsAppNotificationService {

    private static final String GRAPH_API_BASE = "https://graph.facebook.com/v21.0";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Value("${app.whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${app.whatsapp.access-token:}")
    private String accessToken;

    @Value("${app.whatsapp.template.encomenda:encomenda_na_portaria}")
    private String encomendaTemplateName;

    @Value("${app.whatsapp.base-url:}")
    private String baseUrl;

    /**
     * Notifica o morador que tem encomenda na portaria (template com nome e código).
     * O número deve ser E.164 sem + (ex: 5511999999999).
     */
    public void sendEncomendaNotification(String toPhone, String residentName, String residentCode) {
        if (toPhone == null || toPhone.isBlank()) {
            log.debug("WhatsApp: telefone vazio, nao enviando notificacao de encomenda");
            return;
        }
        String phone = normalizePhone(toPhone);
        if (phone == null) {
            log.warn("WhatsApp: telefone invalido {}", toPhone);
            return;
        }

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", phone,
                "type", "template",
                "template", Map.of(
                        "name", encomendaTemplateName,
                        "language", Map.of("code", "pt_BR"),
                        "components", List.of(
                                Map.of(
                                        "type", "body",
                                        "parameters", List.of(
                                                Map.of("type", "text", "text", residentName != null ? residentName : "Morador"),
                                                Map.of("type", "text", "text", residentCode != null ? residentCode : "")
                                        )
                                )
                        )
                )
        );
        postMessages(body);
    }

    /**
     * Envia a foto da encomenda para o morador (após o template, na janela de 24h).
     * imageUrl deve ser acessível publicamente (ex: https://seu-dominio.com/uploads/...).
     */
    public void sendEncomendaPhoto(String toPhone, String imageUrl) {
        if (toPhone == null || toPhone.isBlank() || imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        String phone = normalizePhone(toPhone);
        if (phone == null) return;

        // Se for path relativo (/uploads/...), tornar absoluto com baseUrl
        String url = imageUrl;
        if (url.startsWith("/") && baseUrl != null && !baseUrl.isBlank()) {
            url = baseUrl.replaceAll("/$", "") + url;
        }

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", phone,
                "type", "image",
                "image", Map.of("link", url)
        );
        postMessages(body);
    }

    private void postMessages(Map<String, Object> body) {
        if (phoneNumberId == null || phoneNumberId.isBlank() || accessToken == null || accessToken.isBlank()) {
            log.warn("WhatsApp: phone-number-id ou access-token nao configurado");
            return;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            String json = objectMapper.writeValueAsString(body);
            HttpEntity<String> entity = new HttpEntity<>(json, headers);
            String url = GRAPH_API_BASE + "/" + phoneNumberId + "/messages";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("WhatsApp API respondeu {}: {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Erro ao enviar mensagem WhatsApp", e);
        }
    }

    /** Remove + e espaços; espera número com DDI (ex: 55 para Brasil). */
    private static String normalizePhone(String phone) {
        if (phone == null) return null;
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.length() < 10) return null;
        if (!digits.startsWith("55") && digits.length() >= 11) {
            digits = "55" + digits;
        } else if (!digits.startsWith("55")) {
            digits = "55" + digits;
        }
        return digits;
    }
}
