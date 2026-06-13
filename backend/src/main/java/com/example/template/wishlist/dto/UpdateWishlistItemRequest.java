package com.example.template.wishlist.dto;

import com.example.template.wishlist.entity.WishlistPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record UpdateWishlistItemRequest(

        @NotBlank
        @Size(max = 255)
        String name,

        @NotBlank
        @URL
        String productUrl,

        String notes,

        WishlistPriority priority,

        Boolean isPurchased
) {}
