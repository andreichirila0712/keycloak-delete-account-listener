package com.andreichirila.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.stream.JsonParsingException;
import org.keycloak.events.Event;
import org.keycloak.events.EventType;
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

public class WebhookSender {
    private final static Logger logger = LoggerFactory.getLogger(WebhookSender.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void sendWebhook(Event event, final String webhookUrl, final String secret) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            logger.error("Webhook could not be retrieved, configuration is missing");
            return;
        }

        Map<String, String> payload = new HashMap<>();

        switch (event.getType()) {
            case EventType.REGISTER -> payload.put("eventType", EventType.REGISTER.toString());
            case EventType.DELETE_ACCOUNT -> payload.put("eventType", EventType.DELETE_ACCOUNT.toString());
            case EventType.VERIFY_EMAIL -> payload.put("eventType", EventType.VERIFY_EMAIL.toString());
        }
        payload.put("userId", event.getUserId());
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
        } catch (JsonParsingException e) {
            logger.error("Failed to serialize payload", e);
        } catch (InterruptedException | IOException e) {
            logger.error("Failed to send webhook for this event. Event type: {}", event.getType(), e);
        }
    }
}
