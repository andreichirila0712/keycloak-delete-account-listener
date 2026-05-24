package com.andreichirila.keycloak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.models.KeycloakSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class DeleteListenerProvider implements EventListenerProvider {
    private static final Logger logger = LoggerFactory.getLogger(DeleteListenerProvider.class);
    private final KeycloakSession session;
    private final String webhookUrl;
    private final String secret;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient client;

    public DeleteListenerProvider(KeycloakSession session, String webhookUrl, String secret) {
        this.session = session;
        this.webhookUrl = webhookUrl;
        this.secret = secret;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Override
    public void onEvent(Event event) {
        if (event.getType() == EventType.DELETE_ACCOUNT) {
            sendWebhook(event);
        }
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {

    }

    @Override
    public void close() {

    }

    private void sendWebhook(Event event) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            logger.error("Webhook could not be retrieved, configuration is missing");
            return;
        }

        Map<String, String> payload = new HashMap<>();
        payload.put("eventType", EventType.DELETE_ACCOUNT.toString());
        payload.put("userId", event.getUserId());
        payload.put("realmId", event.getRealmId());
        payload.put("timestamp", Instant.ofEpochMilli(event.getTime()).toString());

        try {
            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder(URI.create(webhookUrl))
                    .header("Content-Type", "application/json")
                    .header("X-Webhook-Secret", secret)
                    .header("ngrok-skip-browser-warning", "true")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize DELETE_ACCOUNT payload", e);
        } catch (InterruptedException | IOException e) {
            logger.error("Failed to send webhook for user deletion. UserId: {}", event.getUserId(), e);
        }
    }
}
