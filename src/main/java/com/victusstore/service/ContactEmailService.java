package com.victusstore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victusstore.dto.ContactMessageRequest;
import com.victusstore.model.StoreSettings;
import com.victusstore.repository.StoreSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class ContactEmailService {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Autowired
    private StoreSettingsRepository storeSettingsRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.resend.api-key:re_WWAajV9V_9dUHS2ggT2ANV6gqJyFQ5GjT}")
    private String resendApiKey;

    @Value("${app.resend.from-email:onboarding@resend.dev}")
    private String resendFromEmail;

    public void sendContactEmail(ContactMessageRequest request) {
        if (resendApiKey == null || resendApiKey.isBlank() || "re_xxxxxxxxx".equals(resendApiKey)) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Resend API key is not configured. Replace re_xxxxxxxxx with your real API key."
            );
        }

        StoreSettings settings = storeSettingsRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Website settings are not configured"
                ));

        if (settings.getStoreEmail() == null || settings.getStoreEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Website email is missing from website settings"
            );
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("from", resendFromEmail);
        payload.put("to", settings.getStoreEmail().trim());
        payload.put("reply_to", request.getEmail().trim());
        payload.put("subject", "[Contact] " + request.getSubject().trim());
        payload.put("html", buildHtml(request, settings));
        payload.put("text", buildText(request, settings));

        try {
            String requestBody = objectMapper.writeValueAsString(payload);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + resendApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Failed to send contact email via Resend"
                );
            }
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Failed to send contact email via Resend"
            );
        }
    }

    private String buildHtml(ContactMessageRequest request, StoreSettings settings) {
        return "<div style=\"font-family:Arial,sans-serif;line-height:1.6;color:#111\">"
                + "<h2>New contact message for " + escapeHtml(settings.getStoreName()) + "</h2>"
                + "<p><strong>Name:</strong> " + escapeHtml(request.getName()) + "</p>"
                + "<p><strong>Email:</strong> " + escapeHtml(request.getEmail()) + "</p>"
                + "<p><strong>Subject:</strong> " + escapeHtml(request.getSubject()) + "</p>"
                + "<p><strong>Message:</strong></p>"
                + "<div style=\"padding:12px;border:1px solid #ddd;border-radius:8px;white-space:pre-wrap;\">"
                + escapeHtml(request.getMessage())
                + "</div>"
                + "</div>";
    }

    private String buildText(ContactMessageRequest request, StoreSettings settings) {
        return "New contact message for " + safe(settings.getStoreName()) + "\n\n"
                + "Name: " + safe(request.getName()) + "\n"
                + "Email: " + safe(request.getEmail()) + "\n"
                + "Subject: " + safe(request.getSubject()) + "\n\n"
                + safe(request.getMessage());
    }

    private String escapeHtml(String value) {
        return safe(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
