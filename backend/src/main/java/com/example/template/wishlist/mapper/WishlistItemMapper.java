package com.example.template.wishlist.mapper;

import com.example.template.wishlist.dto.WishlistItemResponse;
import com.example.template.wishlist.entity.WishlistItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface WishlistItemMapper {

    @Mapping(target = "isPurchased", source = "purchased")
    WishlistItemResponse toDto(WishlistItem item);
}
