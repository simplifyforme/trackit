package com.example.template.order.dto;

import com.example.template.order.entity.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateOrderRequest(

        @NotBlank
        @Size(max = 255)
        String title,

        String description,

        @Size(max = 255)
        String merchant,

        BigDecimal amount,

        @Size(max = 10)
        String currency,

        OrderStatus status,

        @Size(max = 255)
        String externalRef,

        Instant orderDate
) {}
