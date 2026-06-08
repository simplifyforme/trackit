package com.example.template.integrations.gmail;

import com.example.template.exception.ApiException;
import com.example.template.user.entity.User;
import com.example.template.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the Gmail OAuth2 authorisation flow.
 * Completely independent of the app's own authentication system.
 *
 * Flow:
 *   1. POST /api/gmail/connect  → returns { "authorizationUrl": "..." }
 *   2. User opens that URL in a browser and grants permission.
 *   3. Google redirects to GET /api/gmail/callback?code=...&state=...
 *   4. Backend exchanges the code, stores encrypted tokens, shows success page.
 */
@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
@Slf4j
public class GmailOAuthController {

    /**
     * In-memory state map: state token → user ID.
     * TTL of 10 minutes enforced manually.
     * Assumption: acceptable for single-instance deployments; use a shared cache for multi-node.
     */
    private final ConcurrentHashMap<String, PendingState> pendingStates = new ConcurrentHashMap<>();

    private final GmailService gmailService;
    private final UserRepository userRepository;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @PostMapping("/connect")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Map<String, String> initiateConnection(@AuthenticationPrincipal UserDetails userDetails) {
        User user = requireUser(userDetails.getUsername());
        String state = UUID.randomUUID().toString();
        pendingStates.put(state, new PendingState(user.getId(), System.currentTimeMillis()));
        cleanStaleStates();
        String authUrl = gmailService.buildAuthorizationUrl(state);
        return Map.of("authorizationUrl", authUrl);
    }

    /** Called by Google after user grants permission. Not protected by JWT — this is the OAuth callback. */
    @GetMapping("/callback")
    public ResponseEntity<String> handleCallback(
            @RequestParam String code,
            @RequestParam String state,
            @RequestParam(required = false) String error) {

        if (error != null) {
            log.warn("Gmail OAuth denied by user: {}", error);
            return htmlResponse("Gmail connection cancelled. You can close this tab.");
        }

        PendingState pending = pendingStates.remove(state);
        if (pending == null || pending.isExpired()) {
            log.warn("Gmail OAuth callback with invalid/expired state: {}", state);
            return htmlResponse("Link expired or invalid. Please try connecting again from the app.");
        }

        User user = userRepository.findById(pending.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        try {
            gmailService.exchangeCodeAndStore(user, code);
            log.info("Gmail connected for user {}", user.getId());
        } catch (Exception e) {
            log.error("Failed to exchange Gmail OAuth code for user {}: {}", user.getId(), e.getMessage());
            return htmlResponse("Failed to connect Gmail. Please try again.");
        }

        return htmlResponse("Gmail connected successfully! You can close this tab and return to the app.");
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Map<String, Boolean> status(@AuthenticationPrincipal UserDetails userDetails) {
        User user = requireUser(userDetails.getUsername());
        return Map.of("connected", gmailService.isConnected(user));
    }

    @DeleteMapping("/disconnect")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Map<String, String> disconnect(@AuthenticationPrincipal UserDetails userDetails) {
        User user = requireUser(userDetails.getUsername());
        gmailService.disconnect(user);
        return Map.of("message", "Gmail account disconnected");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private ResponseEntity<String> htmlResponse(String message) {
        String html = """
                <!DOCTYPE html><html><head><meta charset="utf-8">
                <title>Trackit – Gmail</title>
                <style>body{font-family:sans-serif;display:flex;justify-content:center;
                align-items:center;height:100vh;margin:0;background:#f5f5f5;}
                .box{background:#fff;padding:40px;border-radius:12px;
                text-align:center;box-shadow:0 2px 16px rgba(0,0,0,.1);}</style>
                </head><body><div class="box"><h2>%s</h2></div></body></html>
                """.formatted(message);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    private void cleanStaleStates() {
        long cutoff = System.currentTimeMillis() - 600_000; // 10 minutes
        pendingStates.entrySet().removeIf(e -> e.getValue().createdAt() < cutoff);
    }

    private record PendingState(UUID userId, long createdAt) {
        boolean isExpired() { return System.currentTimeMillis() - createdAt > 600_000; }
    }
}
