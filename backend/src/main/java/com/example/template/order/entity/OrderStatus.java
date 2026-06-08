package com.example.template.order.entity;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED,
    RETURNED,
    /** AI classification flagged this email for human review. */
    NEEDS_REVIEW
}
