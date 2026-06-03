package com.andreichirila.keycloak;

import com.andreichirila.utils.WebhookSender;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;

public class RegisterListenerProvider implements EventListenerProvider {
    private final KeycloakSession session;
    private final String webhookUrl;
    private final String secret;

    public RegisterListenerProvider(KeycloakSession session, String webhookUrl, String secret) {
        this.session = session;
        this.webhookUrl = webhookUrl;
        this.secret = secret;
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.REGISTER) {
            WebhookSender.sendWebhook(event, webhookUrl, secret);
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {

    }

    @Override
    public void close() {

    }
}
