package com.example.template.integrations.gmail;

import com.example.template.config.EncryptionService;
import com.example.template.integrations.gmail.entity.GmailSyncCursor;
import com.example.template.integrations.gmail.entity.GmailToken;
import com.example.template.integrations.gmail.repository.GmailSyncCursorRepository;
import com.example.template.integrations.gmail.repository.GmailTokenRepository;
import com.example.template.user.entity.User;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.*;

/**
 * Manages Gmail OAuth2 tokens (no relationship to the app's own auth system)
 * and provides helper methods to call the Gmail REST API on behalf of a user.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GmailService {

    private static final String TOKEN_ENDPOINT  = "https://oauth2.googleapis.com/token";
    private static final String PROFILE_URL     = "https://gmail.googleapis.com/gmail/v1/users/me/profile";
    private static final String MESSAGES_URL    = "https://gmail.googleapis.com/gmail/v1/users/me/messages";
    private static final String MESSAGE_URL     = "https://gmail.googleapis.com/gmail/v1/users/me/messages/{id}";
    private static final String HISTORY_URL     = "https://gmail.googleapis.com/gmail/v1/users/me/history";

    @Value("${app.gmail.client-id:}")
    private String clientId;

    @Value("${app.gmail.client-secret:}")
    private String clientSecret;

    @Value("${app.gmail.redirect-uri:http://localhost:8080/api/gmail/callback}")
    private String redirectUri;

    private final GmailTokenRepository tokenRepository;
    private final GmailSyncCursorRepository cursorRepository;
    private final EncryptionService encryptionService;
    private final ObjectMapper objectMapper;

    // ─── OAuth2 flow ─────────────────────────────────────────────────────────

    public String buildAuthorizationUrl(String state) {
        return UriComponentsBuilder
                .fromHttpUrl("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "https://www.googleapis.com/auth/gmail.readonly")
                .queryParam("access_type", "offline")
                .queryParam("prompt", "select_account consent")
                .queryParam("state", state)
                .toUriString();
    }

    @Transactional
    public void exchangeCodeAndStore(User user, String code) {
        Map<String, String> params = Map.of(
                "code", code,
                "client_id", clientId,
                "client_secret", clientSecret,
                "redirect_uri", redirectUri,
                "grant_type", "authorization_code"
        );
        JsonNode tokenResponse = postForm(TOKEN_ENDPOINT, params);
        storeTokens(user, tokenResponse);
        fetchAndStoreProfile(user);
    }

    // ─── Token management ────────────────────────────────────────────────────

    /** Returns a valid (possibly refreshed) access token for the user, or empty. */
    @Transactional
    public Optional<String> getValidAccessToken(User user) {
        return tokenRepository.findByUser(user).flatMap(token -> {
            if (token.getExpiresAt() != null && Instant.now().isAfter(token.getExpiresAt().minusSeconds(60))) {
                return refreshAccessToken(token);
            }
            return Optional.of(encryptionService.decrypt(token.getAccessToken()));
        });
    }

    private Optional<String> refreshAccessToken(GmailToken token) {
        if (token.getRefreshToken() == null) {
            log.warn("No refresh token for user {}; Gmail connection needs re-authorisation", token.getUser().getId());
            return Optional.empty();
        }
        try {
            Map<String, String> params = Map.of(
                    "refresh_token", encryptionService.decrypt(token.getRefreshToken()),
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "grant_type", "refresh_token"
            );
            JsonNode resp = postForm(TOKEN_ENDPOINT, params);
            String newAccessToken = resp.path("access_token").asText();
            int expiresIn = resp.path("expires_in").asInt(3600);
            token.setAccessToken(encryptionService.encrypt(newAccessToken));
            token.setExpiresAt(Instant.now().plusSeconds(expiresIn));
            tokenRepository.save(token);
            return Optional.of(newAccessToken);
        } catch (Exception e) {
            log.error("Failed to refresh Gmail access token for user {}: {}", token.getUser().getId(), e.getMessage());
            return Optional.empty();
        }
    }

    // ─── Gmail API helpers ───────────────────────────────────────────────────

    /** Fetches message IDs added since the stored historyId. Falls back to recent messages on first sync. */
    public List<String> fetchNewMessageIds(User user, String accessToken) {
        Optional<GmailSyncCursor> cursorOpt = cursorRepository.findByUser(user);

        if (cursorOpt.isPresent() && cursorOpt.get().getHistoryId() != null) {
            return fetchViaHistory(accessToken, cursorOpt.get().getHistoryId());
        }
        return fetchRecentMessageIds(accessToken);
    }

    private List<String> fetchViaHistory(String accessToken, String startHistoryId) {
        try {
            String url = UriComponentsBuilder.fromUriString(HISTORY_URL)
                    .queryParam("startHistoryId", startHistoryId)
                    .queryParam("historyTypes", "messageAdded")
                    .toUriString();
            String body = bearerGet(url, accessToken);
            JsonNode root = objectMapper.readTree(body);
            List<String> ids = new ArrayList<>();
            root.path("history").forEach(h ->
                    h.path("messagesAdded").forEach(m ->
                            ids.add(m.path("message").path("id").asText())));
            return ids;
        } catch (Exception e) {
            log.warn("History fetch failed (historyId may be expired), falling back to recent: {}", e.getMessage());
            return fetchRecentMessageIds(accessToken);
        }
    }

    private List<String> fetchRecentMessageIds(String accessToken) {
        try {
            String url = UriComponentsBuilder.fromUriString(MESSAGES_URL)
                    .queryParam("maxResults", 50)
                    .queryParam("q", "in:inbox newer_than:30d")
                    .toUriString();
            String body = bearerGet(url, accessToken);
            JsonNode root = objectMapper.readTree(body);
            List<String> ids = new ArrayList<>();
            root.path("messages").forEach(m -> ids.add(m.path("id").asText()));
            return ids;
        } catch (Exception e) {
            log.error("Failed to fetch recent Gmail message IDs: {}", e.getMessage());
            return List.of();
        }
    }

    /** Fetches full message content and extracts the plain-text body. */
    public Optional<GmailMessage> fetchMessage(String messageId, String accessToken) {
        try {
            String url = UriComponentsBuilder.fromUriString(MESSAGE_URL)
                    .queryParam("format", "full")
                    .buildAndExpand(messageId).toUriString();
            String body = bearerGet(url, accessToken);
            JsonNode root = objectMapper.readTree(body);

            String historyId = root.path("historyId").asText(null);
            String subject = extractHeader(root, "Subject");
            String from    = extractHeader(root, "From");
            String text    = extractBody(root.path("payload"));

            return Optional.of(new GmailMessage(messageId, historyId, subject, from, text));
        } catch (Exception e) {
            log.warn("Failed to fetch Gmail message {}: {}", messageId, e.getMessage());
            return Optional.empty();
        }
    }

    @Transactional
    public void updateCursor(User user, String historyId) {
        GmailSyncCursor cursor = cursorRepository.findByUser(user)
                .orElseGet(() -> GmailSyncCursor.builder().user(user).build());
        cursor.setHistoryId(historyId);
        cursor.setLastSyncedAt(Instant.now());
        cursorRepository.save(cursor);
    }

    public boolean isConnected(User user) {
        return tokenRepository.findByUser(user).isPresent();
    }

    @Transactional
    public void disconnect(User user) {
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
        cursorRepository.findByUser(user).ifPresent(cursorRepository::delete);
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private void storeTokens(User user, JsonNode resp) {
        GmailToken token = tokenRepository.findByUser(user)
                .orElseGet(() -> GmailToken.builder().user(user).build());
        token.setAccessToken(encryptionService.encrypt(resp.path("access_token").asText()));
        if (resp.has("refresh_token")) {
            token.setRefreshToken(encryptionService.encrypt(resp.path("refresh_token").asText()));
        }
        token.setTokenType(resp.path("token_type").asText("Bearer"));
        token.setScope(resp.path("scope").asText());
        int expiresIn = resp.path("expires_in").asInt(3600);
        token.setExpiresAt(Instant.now().plusSeconds(expiresIn));
        tokenRepository.save(token);
    }

    private void fetchAndStoreProfile(User user) {
        try {
            String accessToken = encryptionService.decrypt(
                    tokenRepository.findByUser(user).orElseThrow().getAccessToken());
            String body = bearerGet(PROFILE_URL, accessToken);
            JsonNode profile = objectMapper.readTree(body);
            String gmailEmail = profile.path("emailAddress").asText(null);
            String historyId  = profile.path("historyId").asText(null);
            tokenRepository.findByUser(user).ifPresent(t -> {
                t.setGmailEmail(gmailEmail);
                tokenRepository.save(t);
            });
            if (historyId != null) {
                updateCursor(user, historyId);
            }
        } catch (Exception e) {
            log.warn("Failed to fetch Gmail profile: {}", e.getMessage());
        }
    }

    private String bearerGet(String url, String accessToken) {
        return RestClient.create().get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(String.class);
    }

    private JsonNode postForm(String url, Map<String, String> params) {
        try {
            StringBuilder formBody = new StringBuilder();
            params.forEach((k, v) -> {
                if (!formBody.isEmpty()) formBody.append('&');
                formBody.append(k).append('=').append(v);
            });
            String response = RestClient.create().post()
                    .uri(url)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(formBody.toString())
                    .retrieve()
                    .body(String.class);
            return objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Token endpoint call failed: " + e.getMessage(), e);
        }
    }

    private String extractHeader(JsonNode messageRoot, String name) {
        return messageRoot.path("payload").path("headers").findValues("name").stream()
                .filter(n -> name.equalsIgnoreCase(n.asText()))
                .findFirst()
                .map(n -> {
                    // sibling "value" node — walk up and find value field
                    JsonNode headers = messageRoot.path("payload").path("headers");
                    for (JsonNode h : headers) {
                        if (name.equalsIgnoreCase(h.path("name").asText())) {
                            return h.path("value").asText("");
                        }
                    }
                    return "";
                })
                .orElse("");
    }

    private String extractBody(JsonNode payload) {
        // Try text/plain part first, then text/html
        String plain = extractPartByMime(payload, "text/plain");
        if (plain != null && !plain.isBlank()) return plain;
        String html = extractPartByMime(payload, "text/html");
        return html != null ? stripHtml(html) : "";
    }

    private String extractPartByMime(JsonNode payload, String mimeType) {
        if (mimeType.equals(payload.path("mimeType").asText())) {
            String data = payload.path("body").path("data").asText(null);
            if (data != null) return new String(Base64.getUrlDecoder().decode(data));
        }
        for (JsonNode part : payload.path("parts")) {
            String result = extractPartByMime(part, mimeType);
            if (result != null) return result;
        }
        return null;
    }

    private String stripHtml(String html) {
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s{2,}", " ").trim();
    }

    /** Value object carrying the data extracted from a single Gmail message. */
    public record GmailMessage(
            String messageId,
            String historyId,
            String subject,
            String from,
            String body
    ) {}
}
