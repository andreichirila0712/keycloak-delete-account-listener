package com.andreichirila.keycloak;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class VerifyEmailListenerProviderFactory implements EventListenerProviderFactory {
    private String webhook;
    private String verifyEmailSecret;

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new VerifyEmailListenerProvider(keycloakSession, webhook, verifyEmailSecret);
    }

    @Override
    public void init(Config.Scope config) {
        this.webhook = config.get("webhook-verify-url");
        this.verifyEmailSecret = config.get("webhook-verify-secret");
    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "application-verify-webhook";
    }
}
