package com.example.template.wishlist.dto;

import com.example.template.wishlist.entity.WishlistPriority;

import java.time.Instant;
import java.util.UUID;

public record WishlistItemResponse(
        UUID id,
        String name,
        String productUrl,
        String imageUrl,
        String notes,
        WishlistPriority priority,
        boolean isPurchased,
        Instant createdAt,
        Instant updatedAt
) {}
