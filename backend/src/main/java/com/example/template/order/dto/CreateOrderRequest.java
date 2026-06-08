package com.example.template.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateOrderRequest(

        @NotBlank
        @Size(max = 255)
        String title,

        String description,

        @Size(max = 255)
        String merchant,

        BigDecimal amount,

        @Size(max = 10)
        String currency,

        @Size(max = 255)
        String externalRef,

        Instant orderDate
) {}
