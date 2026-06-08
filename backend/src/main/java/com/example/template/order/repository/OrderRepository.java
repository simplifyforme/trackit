package com.example.template.order.repository;

import com.example.template.order.entity.Order;
import com.example.template.order.entity.OrderStatus;
import com.example.template.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findAllByUser(User user);

    List<Order> findAllByUserAndStatus(User user, OrderStatus status);

    Optional<Order> findByIdAndUser(UUID id, User user);

    Optional<Order> findByUserAndGmailMessageId(User user, String gmailMessageId);

    Optional<Order> findByUserAndExternalRef(User user, String externalRef);
}
