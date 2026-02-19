package com.convivium.integration.whatsapp;

/**
 * Serviço de notificação por WhatsApp (Meta Cloud API).
 * Implementações: MetaWhatsAppService (ativa) ou NoOpWhatsAppService (desativado).
 */
public interface WhatsAppNotificationService {

    void sendEncomendaNotification(String toPhone, String residentName, String residentCode);

    void sendEncomendaPhoto(String toPhone, String imageUrl);
}
