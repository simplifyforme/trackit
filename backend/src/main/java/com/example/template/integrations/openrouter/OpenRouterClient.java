package com.example.template.integrations.openrouter;

import com.example.template.integrations.openrouter.dto.EmailClassificationResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Thin HTTP wrapper around the OpenRouter chat-completions endpoint.
 * Responsible only for the network call and raw JSON parsing.
 * Business logic (retry, validation, fallback) lives in EmailClassificationService.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OpenRouterClient {

    private static final String COMPLETIONS_URL = "https://openrouter.ai/api/v1/chat/completions";

    private static final String SYSTEM_PROMPT = """
            You are an email classifier for an order-tracking application.
            Analyse the given email and respond ONLY with a valid JSON object — no prose, no markdown fences.
            The JSON must match this schema exactly:
            {
              "is_order_email": boolean,
              "intent": "new_order" | "status_update" | "not_relevant",
              "confidence": number between 0 and 1,
              "external_ref": string or null,
              "merchant": string or null,
              "amount": number or null,
              "currency": string or null,
              "new_status": "PENDING"|"CONFIRMED"|"SHIPPED"|"OUT_FOR_DELIVERY"|"DELIVERED"|"CANCELLED"|"RETURNED" or null,
              "summary": string (one sentence)
            }
            Rules:
            - Set is_order_email=false and intent="not_relevant" for newsletters, promotions, and unrelated emails.
            - Set intent="new_order" only when the email confirms a new purchase.
            - Set intent="status_update" when the email updates the status of an existing order.
            - external_ref is the order/confirmation number extracted verbatim from the email, or null.
            - confidence reflects how certain you are (0 = complete guess, 1 = obvious).
            """;

    private final ObjectMapper objectMapper;

    @Value("${app.openrouter.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    /**
     * Calls the completions endpoint and returns the raw classification JSON string.
     * Returns empty if the model produced no output.
     */
    public Optional<String> classify(String emailText, String apiKey, String model) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user", "content", "Email to classify:\n\n" + emailText)
                ),
                "response_format", Map.of("type", "json_object")
        );

        try {
            RestClient client = RestClient.create();
            String responseBody = client.post()
                    .uri(COMPLETIONS_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .header("HTTP-Referer", "https://trackit-app")
                    .header("X-Title", "Trackit Order Tracker")
                    .body(objectMapper.writeValueAsString(body))
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            String content = root.path("choices").path(0).path("message").path("content").asText(null);
            return Optional.ofNullable(content);
        } catch (Exception e) {
            log.warn("OpenRouter API call failed: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
