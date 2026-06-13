package com.example.template.wishlist.service;

import com.example.template.exception.ApiException;
import com.example.template.user.entity.User;
import com.example.template.user.repository.UserRepository;
import com.example.template.wishlist.dto.CreateWishlistItemRequest;
import com.example.template.wishlist.dto.UpdateWishlistItemRequest;
import com.example.template.wishlist.dto.WishlistItemResponse;
import com.example.template.wishlist.entity.WishlistItem;
import com.example.template.wishlist.entity.WishlistPriority;
import com.example.template.wishlist.mapper.WishlistItemMapper;
import com.example.template.wishlist.repository.WishlistItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final UserRepository userRepository;
    private final WishlistItemMapper wishlistItemMapper;
    private final ProductImageScraperService imageScraper;

    @Transactional(readOnly = true)
    public List<WishlistItemResponse> list(String email) {
        User user = requireUser(email);
        return wishlistItemRepository.findAllByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(wishlistItemMapper::toDto)
                .toList();
    }

    @Transactional
    public WishlistItemResponse create(String email, CreateWishlistItemRequest req) {
        User user = requireUser(email);
        String imageUrl = imageScraper.scrape(req.productUrl());
        WishlistItem item = WishlistItem.builder()
                .user(user)
                .name(req.name())
                .productUrl(req.productUrl())
                .imageUrl(imageUrl)
                .notes(req.notes())
                .priority(req.priority() != null ? req.priority() : WishlistPriority.MEDIUM)
                .build();
        return wishlistItemMapper.toDto(wishlistItemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public WishlistItemResponse get(String email, UUID id) {
        User user = requireUser(email);
        return wishlistItemMapper.toDto(requireOwned(id, user));
    }

    @Transactional
    public WishlistItemResponse update(String email, UUID id, UpdateWishlistItemRequest req) {
        User user = requireUser(email);
        WishlistItem item = requireOwned(id, user);

        boolean urlChanged = !item.getProductUrl().equals(req.productUrl());

        item.setName(req.name());
        item.setProductUrl(req.productUrl());
        item.setNotes(req.notes());
        if (req.priority() != null) item.setPriority(req.priority());
        if (req.isPurchased() != null) item.setPurchased(req.isPurchased());

        // Re-scrape image only when the product URL changes
        if (urlChanged) {
            item.setImageUrl(imageScraper.scrape(req.productUrl()));
        }

        return wishlistItemMapper.toDto(wishlistItemRepository.save(item));
    }

    @Transactional
    public void delete(String email, UUID id) {
        User user = requireUser(email);
        WishlistItem item = requireOwned(id, user);
        wishlistItemRepository.delete(item);
    }

    @Transactional
    public WishlistItemResponse refreshImage(String email, UUID id) {
        User user = requireUser(email);
        WishlistItem item = requireOwned(id, user);
        item.setImageUrl(imageScraper.scrape(item.getProductUrl()));
        return wishlistItemMapper.toDto(wishlistItemRepository.save(item));
    }

    private User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private WishlistItem requireOwned(UUID id, User user) {
        return wishlistItemRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Wishlist item not found"));
    }
}
