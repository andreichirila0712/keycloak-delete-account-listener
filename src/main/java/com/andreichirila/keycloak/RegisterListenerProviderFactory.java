package com.andreichirila.keycloak;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class RegisterListenerProviderFactory implements EventListenerProviderFactory {
    private String webhookUrl;
    private String secret;

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new RegisterListenerProvider(keycloakSession, webhookUrl, secret);
    }

    @Override
    public void init(Config.Scope config) {
        this.webhookUrl = config.get("webhook-register-url");
        this.secret = config.get("webhook-register-secret");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "application-register-webhook";
    }
}
