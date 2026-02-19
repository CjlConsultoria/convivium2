package com.convivium.integration.whatsapp;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.whatsapp.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpWhatsAppService implements WhatsAppNotificationService {

    @Override
    public void sendEncomendaNotification(String toPhone, String residentName, String residentCode) {
        // WhatsApp desativado
    }

    @Override
    public void sendEncomendaPhoto(String toPhone, String imageUrl) {
        // WhatsApp desativado
    }
}
