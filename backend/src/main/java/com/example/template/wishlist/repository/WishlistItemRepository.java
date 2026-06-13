package com.example.template.wishlist.repository;

import com.example.template.user.entity.User;
import com.example.template.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {

    List<WishlistItem> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<WishlistItem> findByIdAndUser(UUID id, User user);
}
