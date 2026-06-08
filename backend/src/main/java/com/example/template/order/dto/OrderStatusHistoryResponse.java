package com.example.template.order.dto;

import com.example.template.order.entity.OrderSource;
import com.example.template.order.entity.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusHistoryResponse(
        UUID id,
        OrderStatus oldStatus,
        OrderStatus newStatus,
        Instant changedAt,
        OrderSource source,
        String note
) {}
