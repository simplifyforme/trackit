package com.example.template.integrations.openrouter;

import com.example.template.integrations.openrouter.dto.EmailClassificationResult;
import com.example.template.order.entity.OrderStatus;
import com.example.template.settings.service.SettingsService;
import com.example.template.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailClassificationServiceTest {

    @Mock OpenRouterClient openRouterClient;
    @Mock SettingsService settingsService;

    @InjectMocks EmailClassificationService service;

    User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(service, "envApiKey", "test-api-key");
        ReflectionTestUtils.setField(service, "defaultModel", "openrouter/auto");
        lenient().when(settingsService.resolveApiKey(any(), eq("test-api-key"))).thenReturn("test-api-key");
        lenient().when(settingsService.resolveModel(any(), eq("openrouter/auto"))).thenReturn("openrouter/auto");
    }

    @Test
    void classify_parsesValidNewOrderResponse() {
        String json = """
                {
                  "is_order_email": true,
                  "intent": "new_order",
                  "confidence": 0.95,
                  "external_ref": "ORD-12345",
                  "merchant": "Amazon",
                  "amount": 49.99,
                  "currency": "USD",
                  "new_status": "CONFIRMED",
                  "summary": "Amazon order confirmation for ORD-12345"
                }""";
        when(openRouterClient.classify(anyString(), anyString(), anyString())).thenReturn(Optional.of(json));

        Optional<EmailClassificationResult> result = service.classify(user, "email body");

        assertThat(result).isPresent();
        assertThat(result.get().isOrderEmail()).isTrue();
        assertThat(result.get().intent()).isEqualTo("new_order");
        assertThat(result.get().externalRef()).isEqualTo("ORD-12345");
        assertThat(result.get().merchant()).isEqualTo("Amazon");
        assertThat(result.get().newStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void classify_parsesStatusUpdateResponse() {
        String json = """
                {
                  "is_order_email": true,
                  "intent": "status_update",
                  "confidence": 0.88,
                  "external_ref": "ORD-12345",
                  "merchant": "Amazon",
                  "amount": null,
                  "currency": null,
                  "new_status": "SHIPPED",
                  "summary": "Your order has shipped"
                }""";
        when(openRouterClient.classify(anyString(), anyString(), anyString())).thenReturn(Optional.of(json));

        Optional<EmailClassificationResult> result = service.classify(user, "email body");

        assertThat(result).isPresent();
        assertThat(result.get().isStatusUpdate()).isTrue();
        assertThat(result.get().newStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void classify_returnsEmptyForNotRelevant() {
        String json = """
                {
                  "is_order_email": false,
                  "intent": "not_relevant",
                  "confidence": 0.99,
                  "external_ref": null,
                  "merchant": null,
                  "amount": null,
                  "currency": null,
                  "new_status": null,
                  "summary": "Newsletter from a clothing brand"
                }""";
        when(openRouterClient.classify(anyString(), anyString(), anyString())).thenReturn(Optional.of(json));

        Optional<EmailClassificationResult> result = service.classify(user, "email body");

        assertThat(result).isPresent();
        assertThat(result.get().isOrderEmail()).isFalse();
        assertThat(result.get().intent()).isEqualTo("not_relevant");
    }

    @Test
    void classify_returnsEmptyForMalformedJson() {
        when(openRouterClient.classify(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of("not valid json at all"));

        Optional<EmailClassificationResult> result = service.classify(user, "email body");

        assertThat(result).isEmpty();
    }

    @Test
    void classify_returnsEmptyForMissingIntent() {
        String json = """
                {
                  "is_order_email": true,
                  "confidence": 0.9,
                  "summary": "Some order email"
                }""";
        when(openRouterClient.classify(anyString(), anyString(), anyString())).thenReturn(Optional.of(json));

        Optional<EmailClassificationResult> result = service.classify(user, "email body");

        assertThat(result).isEmpty();
    }

    @Test
    void classify_returnsEmptyWhenApiKeyMissing() {
        when(settingsService.resolveApiKey(any(), any())).thenReturn("");

        Optional<EmailClassificationResult> result = service.classify(user, "email body");

        assertThat(result).isEmpty();
    }

    @Test
    void classify_returnsEmptyWhenClientReturnsEmpty() {
        when(openRouterClient.classify(anyString(), anyString(), anyString())).thenReturn(Optional.empty());

        Optional<EmailClassificationResult> result = service.classify(user, "email body");

        assertThat(result).isEmpty();
    }

    @Test
    void classify_rejectsUnknownIntent() {
        String json = """
                {
                  "is_order_email": true,
                  "intent": "unknown_value",
                  "confidence": 0.7,
                  "summary": "Something"
                }""";
        when(openRouterClient.classify(anyString(), anyString(), anyString())).thenReturn(Optional.of(json));

        Optional<EmailClassificationResult> result = service.classify(user, "email body");

        assertThat(result).isEmpty();
    }
}
