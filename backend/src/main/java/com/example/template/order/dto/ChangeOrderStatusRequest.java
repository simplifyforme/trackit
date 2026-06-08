package com.example.template.order.dto;

import com.example.template.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record ChangeOrderStatusRequest(

        @NotNull
        OrderStatus status,

        String note
) {}
