package com.example.template.wishlist.controller;

import com.example.template.wishlist.dto.CreateWishlistItemRequest;
import com.example.template.wishlist.dto.UpdateWishlistItemRequest;
import com.example.template.wishlist.dto.WishlistItemResponse;
import com.example.template.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public List<WishlistItemResponse> list(@AuthenticationPrincipal UserDetails userDetails) {
        return wishlistService.list(userDetails.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public WishlistItemResponse create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateWishlistItemRequest request) {
        return wishlistService.create(userDetails.getUsername(), request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public WishlistItemResponse get(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return wishlistService.get(userDetails.getUsername(), id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public WishlistItemResponse update(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWishlistItemRequest request) {
        return wishlistService.update(userDetails.getUsername(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public void delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        wishlistService.delete(userDetails.getUsername(), id);
    }

    @PostMapping("/{id}/refresh-image")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public WishlistItemResponse refreshImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID id) {
        return wishlistService.refreshImage(userDetails.getUsername(), id);
    }
}
