package com.example.template.order.service;

import com.example.template.exception.ApiException;
import com.example.template.order.dto.*;
import com.example.template.order.entity.*;
import com.example.template.order.mapper.OrderMapper;
import com.example.template.order.repository.OrderRepository;
import com.example.template.order.repository.OrderStatusHistoryRepository;
import com.example.template.user.entity.User;
import com.example.template.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderResponse> list(String email, OrderStatus statusFilter, String sortBy) {
        User user = requireUser(email);
        List<Order> orders = (statusFilter != null)
                ? orderRepository.findAllByUserAndStatus(user, statusFilter)
                : orderRepository.findAllByUser(user);

        Comparator<Order> comparator = "createdAt".equals(sortBy)
                ? Comparator.comparing(Order::getCreatedAt)
                : Comparator.comparing(Order::getCreatedAt).reversed();

        return orders.stream()
                .sorted(comparator)
                .map(o -> toResponseWithHistory(o, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(String email, UUID id) {
        User user = requireUser(email);
        Order order = requireOwned(id, user);
        return toResponseWithHistory(order, true);
    }

    @Transactional
    public OrderResponse create(String email, CreateOrderRequest req) {
        User user = requireUser(email);
        Order order = Order.builder()
                .user(user)
                .title(req.title())
                .description(req.description())
                .merchant(req.merchant())
                .amount(req.amount())
                .currency(req.currency())
                .externalRef(req.externalRef())
                .orderDate(req.orderDate())
                .source(OrderSource.MANUAL)
                .status(OrderStatus.PENDING)
                .build();
        order = orderRepository.save(order);
        appendHistory(order, null, OrderStatus.PENDING, OrderSource.MANUAL, "Order created");
        return toResponseWithHistory(order, true);
    }

    @Transactional
    public OrderResponse update(String email, UUID id, UpdateOrderRequest req) {
        User user = requireUser(email);
        Order order = requireOwned(id, user);

        OrderStatus oldStatus = order.getStatus();

        order.setTitle(req.title());
        order.setDescription(req.description());
        order.setMerchant(req.merchant());
        order.setAmount(req.amount());
        order.setCurrency(req.currency());
        order.setExternalRef(req.externalRef());
        order.setOrderDate(req.orderDate());

        if (req.status() != null && req.status() != oldStatus) {
            order.setStatus(req.status());
            appendHistory(order, oldStatus, req.status(), OrderSource.MANUAL, null);
        }

        order = orderRepository.save(order);
        return toResponseWithHistory(order, true);
    }

    @Transactional
    public OrderResponse changeStatus(String email, UUID id, ChangeOrderStatusRequest req) {
        User user = requireUser(email);
        Order order = requireOwned(id, user);

        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == req.status()) {
            return toResponseWithHistory(order, true);
        }

        order.setStatus(req.status());
        order = orderRepository.save(order);
        appendHistory(order, oldStatus, req.status(), OrderSource.MANUAL, req.note());
        return toResponseWithHistory(order, true);
    }

    @Transactional
    public void delete(String email, UUID id) {
        User user = requireUser(email);
        Order order = requireOwned(id, user);
        orderRepository.delete(order);
    }

    // --- internal helpers ---

    private OrderResponse toResponseWithHistory(Order order, boolean includeHistory) {
        OrderResponse base = orderMapper.toDto(order);
        List<OrderStatusHistoryResponse> history = includeHistory
                ? historyRepository.findAllByOrderOrderByChangedAtAsc(order)
                        .stream().map(orderMapper::toHistoryDto).toList()
                : List.of();
        return new OrderResponse(
                base.id(), base.title(), base.description(), base.merchant(),
                base.amount(), base.currency(), base.status(), base.source(),
                base.externalRef(), base.orderDate(), base.createdAt(), base.updatedAt(),
                history);
    }

    private void appendHistory(Order order, OrderStatus oldStatus, OrderStatus newStatus,
                               OrderSource source, String note) {
        historyRepository.save(OrderStatusHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .source(source)
                .note(note)
                .build());
    }

    User requireUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    Order requireOwned(UUID id, User user) {
        return orderRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }
}
