package com.example.template.integrations.gmail;

import com.example.template.integrations.gmail.repository.GmailTokenRepository;
import com.example.template.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Scheduled service that polls Gmail for all connected users on a fixed interval.
 * Each user's sync is isolated — a failure for one user does not stop others.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GmailSyncService {

    private final GmailTokenRepository gmailTokenRepository;
    private final GmailService gmailService;
    private final EmailOrderProcessor emailOrderProcessor;

    @Scheduled(fixedDelayString = "${app.gmail.poll-interval-ms:300000}")
    public void syncAllUsers() {
        List<User> connectedUsers = gmailTokenRepository.findAll()
                .stream()
                .map(t -> t.getUser())
                .toList();

        if (connectedUsers.isEmpty()) return;

        log.debug("Gmail sync starting for {} connected user(s)", connectedUsers.size());

        for (User user : connectedUsers) {
            try {
                syncUser(user);
            } catch (Exception e) {
                log.error("Gmail sync failed for user {}: {}", user.getId(), e.getMessage(), e);
            }
        }
    }

    private void syncUser(User user) {
        String accessToken = gmailService.getValidAccessToken(user).orElse(null);
        if (accessToken == null) {
            log.warn("No valid access token for user {}; skipping Gmail sync", user.getId());
            return;
        }

        List<String> messageIds = gmailService.fetchNewMessageIds(user, accessToken);
        if (messageIds.isEmpty()) {
            log.debug("No new Gmail messages for user {}", user.getId());
            return;
        }

        log.info("Processing {} new Gmail message(s) for user {}", messageIds.size(), user.getId());
        String latestHistoryId = null;

        for (String messageId : messageIds) {
            try {
                GmailService.GmailMessage message = gmailService.fetchMessage(messageId, accessToken).orElse(null);
                if (message == null) continue;

                if (message.historyId() != null) latestHistoryId = message.historyId();

                emailOrderProcessor.process(user, message);
            } catch (Exception e) {
                log.error("Failed to process Gmail message {} for user {}: {}", messageId, user.getId(), e.getMessage());
                // Continue to next message — per-email isolation
            }
        }

        if (latestHistoryId != null) {
            gmailService.updateCursor(user, latestHistoryId);
        }
    }
}
