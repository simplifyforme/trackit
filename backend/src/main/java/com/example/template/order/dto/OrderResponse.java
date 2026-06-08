package com.example.template.order.dto;

import com.example.template.order.entity.OrderSource;
import com.example.template.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String title,
        String description,
        String merchant,
        BigDecimal amount,
        String currency,
        OrderStatus status,
        OrderSource source,
        String externalRef,
        Instant orderDate,
        Instant createdAt,
        Instant updatedAt,
        List<OrderStatusHistoryResponse> statusHistory
) {}
