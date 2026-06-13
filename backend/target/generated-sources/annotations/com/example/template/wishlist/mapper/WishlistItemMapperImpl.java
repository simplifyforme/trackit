package com.example.template.wishlist.mapper;

import com.example.template.wishlist.dto.WishlistItemResponse;
import com.example.template.wishlist.entity.WishlistItem;
import com.example.template.wishlist.entity.WishlistPriority;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-13T18:06:37+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class WishlistItemMapperImpl implements WishlistItemMapper {

    @Override
    public WishlistItemResponse toDto(WishlistItem item) {
        if ( item == null ) {
            return null;
        }

        boolean isPurchased = false;
        UUID id = null;
        String name = null;
        String productUrl = null;
        String imageUrl = null;
        String notes = null;
        WishlistPriority priority = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        isPurchased = item.isPurchased();
        id = item.getId();
        name = item.getName();
        productUrl = item.getProductUrl();
        imageUrl = item.getImageUrl();
        notes = item.getNotes();
        priority = item.getPriority();
        createdAt = item.getCreatedAt();
        updatedAt = item.getUpdatedAt();

        WishlistItemResponse wishlistItemResponse = new WishlistItemResponse( id, name, productUrl, imageUrl, notes, priority, isPurchased, createdAt, updatedAt );

        return wishlistItemResponse;
    }
}
