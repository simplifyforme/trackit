package com.example.template.integrations.openrouter;

import com.example.template.integrations.openrouter.dto.EmailClassificationResult;
import com.example.template.settings.service.SettingsService;
import com.example.template.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Isolated AI agent module. Classifies an email body and returns a validated
 * EmailClassificationResult. All calls are wrapped with retry + backoff.
 * On failure, returns empty — callers must handle the absent case.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailClassificationService {

    private static final double CONFIDENCE_THRESHOLD = 0.5;
    private static final int    MAX_RETRIES          = 2;
    private static final long   RETRY_DELAY_MS       = 1500;

    private final OpenRouterClient openRouterClient;
    private final SettingsService settingsService;
    private final ObjectMapper objectMapper;

    @Value("${app.openrouter.api-key:}")
    private String envApiKey;

    @Value("${app.openrouter.default-model:openrouter/auto}")
    private String defaultModel;

    /**
     * Classifies an email for the given user.
     * Returns empty when: no API key configured, retries exhausted, or output fails validation.
     */
    public Optional<EmailClassificationResult> classify(User user, String emailText) {
        String apiKey = settingsService.resolveApiKey(user, envApiKey);
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("No OpenRouter API key configured for user {}; skipping classification", user.getId());
            return Optional.empty();
        }
        String model = settingsService.resolveModel(user, defaultModel);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                Optional<String> rawJson = openRouterClient.classify(emailText, apiKey, model);
                if (rawJson.isEmpty()) {
                    log.warn("OpenRouter returned no content (attempt {}/{})", attempt, MAX_RETRIES);
                    sleepBefore(attempt);
                    continue;
                }
                return validate(rawJson.get());
            } catch (Exception e) {
                log.warn("Classification attempt {}/{} failed: {}", attempt, MAX_RETRIES, e.getMessage());
                sleepBefore(attempt);
            }
        }
        return Optional.empty();
    }

    private Optional<EmailClassificationResult> validate(String json) {
        try {
            EmailClassificationResult result = objectMapper.readValue(json, EmailClassificationResult.class);

            // Schema validation
            if (result.intent() == null) {
                log.warn("AI output missing required field 'intent': {}", json);
                return Optional.empty();
            }
            if (!List.of("new_order", "status_update", "not_relevant").contains(result.intent())) {
                log.warn("AI output has unexpected intent '{}': {}", result.intent(), json);
                return Optional.empty();
            }
            if (result.confidence() < 0 || result.confidence() > 1) {
                log.warn("AI output confidence {} out of range: {}", result.confidence(), json);
                return Optional.empty();
            }
            if (result.isOrderEmail() && result.confidence() < CONFIDENCE_THRESHOLD) {
                log.info("AI confidence {:.2f} below threshold {}; flagging for review", result.confidence(), CONFIDENCE_THRESHOLD);
            }
            return Optional.of(result);
        } catch (Exception e) {
            log.warn("Failed to parse AI output as EmailClassificationResult: {} — raw: {}", e.getMessage(), json);
            return Optional.empty();
        }
    }

    private void sleepBefore(int attempt) {
        if (attempt < MAX_RETRIES) {
            try { Thread.sleep(RETRY_DELAY_MS); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
        }
    }

}
