package com.example.template.integrations.gmail;

import com.example.template.integrations.openrouter.EmailClassificationService;
import com.example.template.integrations.openrouter.dto.EmailClassificationResult;
import com.example.template.order.entity.*;
import com.example.template.order.repository.OrderRepository;
import com.example.template.order.repository.OrderStatusHistoryRepository;
import com.example.template.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Processes a single Gmail message for a given user:
 *   1. Classify via AI.
 *   2. If new_order → create Order (source=EMAIL).
 *   3. If status_update → find existing order and update status.
 *   4. If low confidence or parse failure → set status=NEEDS_REVIEW.
 *   5. De-duplicates on gmailMessageId so the same email is never processed twice.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EmailOrderProcessor {

    private static final double REVIEW_CONFIDENCE_THRESHOLD = 0.5;

    private final EmailClassificationService classificationService;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    @Transactional
    public void process(User user, GmailService.GmailMessage message) {
        // De-duplicate: skip if we've already processed this Gmail message
        if (orderRepository.findByUserAndGmailMessageId(user, message.messageId()).isPresent()) {
            log.debug("Skipping already-processed Gmail message {} for user {}", message.messageId(), user.getId());
            return;
        }

        String emailText = buildEmailText(message);
        Optional<EmailClassificationResult> resultOpt = classificationService.classify(user, emailText);

        if (resultOpt.isEmpty()) {
            log.warn("Classification failed for Gmail message {} of user {}; flagging for review",
                    message.messageId(), user.getId());
            createNeedsReviewOrder(user, message, "AI classification failed");
            return;
        }

        EmailClassificationResult result = resultOpt.get();

        if (!result.isOrderEmail()) {
            log.debug("Message {} classified as not-order-related; skipping", message.messageId());
            return;
        }

        if (result.confidence() < REVIEW_CONFIDENCE_THRESHOLD) {
            log.info("Low-confidence classification ({}) for message {}; flagging for review",
                    result.confidence(), message.messageId());
            createNeedsReviewOrder(user, message, "Low AI confidence: " + result.confidence());
            return;
        }

        if (result.isNewOrder()) {
            handleNewOrder(user, message, result);
        } else if (result.isStatusUpdate()) {
            handleStatusUpdate(user, message, result);
        }
    }

    // ─── Handlers ────────────────────────────────────────────────────────────

    private void handleNewOrder(User user, GmailService.GmailMessage message, EmailClassificationResult result) {
        Order order = Order.builder()
                .user(user)
                .title(buildTitle(result, message))
                .description(result.summary())
                .merchant(result.merchant())
                .amount(result.amount() != null ? BigDecimal.valueOf(result.amount()) : null)
                .currency(result.currency())
                .status(result.newStatus() != null ? result.newStatus() : OrderStatus.CONFIRMED)
                .source(OrderSource.EMAIL)
                .externalRef(result.externalRef())
                .gmailMessageId(message.messageId())
                .build();
        order = orderRepository.save(order);
        appendHistory(order, null, order.getStatus(), "Created from email: " + message.subject());
        log.info("Created new order {} from Gmail message {} for user {}", order.getId(), message.messageId(), user.getId());
    }

    private void handleStatusUpdate(User user, GmailService.GmailMessage message, EmailClassificationResult result) {
        Optional<Order> orderOpt = resolveExistingOrder(user, result);

        if (orderOpt.isEmpty()) {
            log.info("Status-update email for unknown order (ref={}, merchant={}); creating needs-review entry",
                    result.externalRef(), result.merchant());
            createNeedsReviewOrder(user, message, "Status update for unmatched order: " + result.summary());
            return;
        }

        Order order = orderOpt.get();
        OrderStatus newStatus = result.newStatus();
        if (newStatus == null || newStatus == order.getStatus()) {
            log.debug("No status change needed for order {} from message {}", order.getId(), message.messageId());
            // Still mark the message so we don't re-process it
            order.setGmailMessageId(message.messageId());
            orderRepository.save(order);
            return;
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setGmailMessageId(message.messageId());
        orderRepository.save(order);
        appendHistory(order, oldStatus, newStatus, "Updated from email: " + message.subject());
        log.info("Updated order {} status {} → {} from Gmail message {}", order.getId(), oldStatus, newStatus, message.messageId());
    }

    private void createNeedsReviewOrder(User user, GmailService.GmailMessage message, String note) {
        Order order = Order.builder()
                .user(user)
                .title("Review required: " + truncate(message.subject(), 200))
                .description(note)
                .status(OrderStatus.NEEDS_REVIEW)
                .source(OrderSource.EMAIL)
                .gmailMessageId(message.messageId())
                .build();
        order = orderRepository.save(order);
        appendHistory(order, null, OrderStatus.NEEDS_REVIEW, note);
    }

    // ─── Matching logic ───────────────────────────────────────────────────────

    private Optional<Order> resolveExistingOrder(User user, EmailClassificationResult result) {
        // 1. Match by external_ref (most stable identifier)
        if (result.externalRef() != null && !result.externalRef().isBlank()) {
            Optional<Order> byRef = orderRepository.findByUserAndExternalRef(user, result.externalRef());
            if (byRef.isPresent()) return byRef;
        }
        // 2. No reliable fallback without a confidence-scored fuzzy match — return empty and let caller decide
        return Optional.empty();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void appendHistory(Order order, OrderStatus oldStatus, OrderStatus newStatus, String note) {
        historyRepository.save(OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .source(OrderSource.EMAIL)
                .note(note)
                .build());
    }

    private String buildEmailText(GmailService.GmailMessage message) {
        return "Subject: " + message.subject() + "\nFrom: " + message.from() + "\n\n" + message.body();
    }

    private String buildTitle(EmailClassificationResult result, GmailService.GmailMessage message) {
        if (result.merchant() != null && result.externalRef() != null) {
            return result.merchant() + " – " + result.externalRef();
        }
        if (result.merchant() != null) return result.merchant() + " order";
        return truncate(message.subject(), 255);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max) : s;
    }
}
