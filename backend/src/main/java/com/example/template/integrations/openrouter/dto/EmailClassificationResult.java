package com.example.template.integrations.openrouter.dto;

import com.example.template.order.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Structured JSON output returned by the AI model for each email classified.
 * All fields except isOrderEmail and intent are nullable — the model may not
 * always have enough signal to populate them.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmailClassificationResult(
        @JsonProperty("is_order_email") boolean isOrderEmail,
        String intent,           // "new_order" | "status_update" | "not_relevant"
        double confidence,       // 0.0 – 1.0
        @JsonProperty("external_ref") String externalRef,
        String merchant,
        Double amount,
        String currency,
        @JsonProperty("new_status") OrderStatus newStatus,
        String summary
) {
    public boolean isNewOrder()     { return "new_order".equals(intent); }
    public boolean isStatusUpdate() { return "status_update".equals(intent); }
}
