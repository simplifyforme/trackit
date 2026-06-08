package com.example.template.integrations.gmail;

import com.example.template.integrations.openrouter.EmailClassificationService;
import com.example.template.integrations.openrouter.dto.EmailClassificationResult;
import com.example.template.order.entity.*;
import com.example.template.order.repository.OrderRepository;
import com.example.template.order.repository.OrderStatusHistoryRepository;
import com.example.template.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailOrderProcessorTest {

    @Mock EmailClassificationService classificationService;
    @Mock OrderRepository orderRepository;
    @Mock OrderStatusHistoryRepository historyRepository;

    @InjectMocks EmailOrderProcessor processor;

    User user;
    GmailService.GmailMessage message;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        message = new GmailService.GmailMessage("msg-001", "12345", "Order Confirmed", "amazon@amazon.com", "Your order ORD-123 has been confirmed.");
    }

    @Test
    void process_skipsAlreadyProcessedMessage() {
        Order existing = Order.builder().id(UUID.randomUUID()).gmailMessageId("msg-001").build();
        when(orderRepository.findByUserAndGmailMessageId(user, "msg-001")).thenReturn(Optional.of(existing));

        processor.process(user, message);

        verify(classificationService, never()).classify(any(), any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void process_createsNewOrderForNewOrderIntent() {
        when(orderRepository.findByUserAndGmailMessageId(user, "msg-001")).thenReturn(Optional.empty());
        EmailClassificationResult result = new EmailClassificationResult(
                true, "new_order", 0.95, "ORD-123", "Amazon", 49.99, "USD", OrderStatus.CONFIRMED, "Order confirmed");
        when(classificationService.classify(eq(user), anyString())).thenReturn(Optional.of(result));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        processor.process(user, message);

        verify(orderRepository).save(argThat(o ->
                o.getSource() == OrderSource.EMAIL &&
                o.getStatus() == OrderStatus.CONFIRMED &&
                "ORD-123".equals(o.getExternalRef()) &&
                "msg-001".equals(o.getGmailMessageId())));
        verify(historyRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    void process_updatesExistingOrderForStatusUpdate() {
        Order existing = Order.builder().id(UUID.randomUUID()).user(user)
                .status(OrderStatus.CONFIRMED).externalRef("ORD-123").build();
        when(orderRepository.findByUserAndGmailMessageId(user, "msg-001")).thenReturn(Optional.empty());
        when(orderRepository.findByUserAndExternalRef(user, "ORD-123")).thenReturn(Optional.of(existing));

        EmailClassificationResult result = new EmailClassificationResult(
                true, "status_update", 0.9, "ORD-123", "Amazon", null, null, OrderStatus.SHIPPED, "Your order has shipped");
        when(classificationService.classify(eq(user), anyString())).thenReturn(Optional.of(result));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        processor.process(user, message);

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.SHIPPED));
        verify(historyRepository).save(argThat(h ->
                h.getOldStatus() == OrderStatus.CONFIRMED &&
                h.getNewStatus() == OrderStatus.SHIPPED));
    }

    @Test
    void process_createsNeedsReviewWhenClassificationFails() {
        when(orderRepository.findByUserAndGmailMessageId(user, "msg-001")).thenReturn(Optional.empty());
        when(classificationService.classify(any(), any())).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        processor.process(user, message);

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.NEEDS_REVIEW));
    }

    @Test
    void process_createsNeedsReviewForLowConfidence() {
        when(orderRepository.findByUserAndGmailMessageId(user, "msg-001")).thenReturn(Optional.empty());
        EmailClassificationResult lowConf = new EmailClassificationResult(
                true, "new_order", 0.3, null, null, null, null, null, "Possible order");
        when(classificationService.classify(any(), any())).thenReturn(Optional.of(lowConf));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        processor.process(user, message);

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.NEEDS_REVIEW));
    }

    @Test
    void process_skipsNotRelevantEmails() {
        when(orderRepository.findByUserAndGmailMessageId(user, "msg-001")).thenReturn(Optional.empty());
        EmailClassificationResult notRelevant = new EmailClassificationResult(
                false, "not_relevant", 0.99, null, null, null, null, null, "Newsletter");
        when(classificationService.classify(any(), any())).thenReturn(Optional.of(notRelevant));

        processor.process(user, message);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void process_createsNeedsReviewWhenStatusUpdateHasNoMatchingOrder() {
        when(orderRepository.findByUserAndGmailMessageId(user, "msg-001")).thenReturn(Optional.empty());
        when(orderRepository.findByUserAndExternalRef(any(), any())).thenReturn(Optional.empty());

        EmailClassificationResult result = new EmailClassificationResult(
                true, "status_update", 0.85, "UNKNOWN-REF", "Amazon", null, null, OrderStatus.SHIPPED, "Shipped");
        when(classificationService.classify(any(), any())).thenReturn(Optional.of(result));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        processor.process(user, message);

        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.NEEDS_REVIEW));
    }
}
