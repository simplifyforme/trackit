package com.example.template.order;

import com.example.template.exception.ApiException;
import com.example.template.order.dto.*;
import com.example.template.order.entity.*;
import com.example.template.order.mapper.OrderMapper;
import com.example.template.order.repository.OrderRepository;
import com.example.template.order.repository.OrderStatusHistoryRepository;
import com.example.template.order.service.OrderService;
import com.example.template.user.entity.User;
import com.example.template.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock OrderStatusHistoryRepository historyRepository;
    @Mock UserRepository userRepository;
    @Mock OrderMapper orderMapper;

    @InjectMocks OrderService orderService;

    User user;
    Order order;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        order = Order.builder()
                .id(UUID.randomUUID())
                .user(user)
                .title("AirPods")
                .status(OrderStatus.PENDING)
                .source(OrderSource.MANUAL)
                .createdAt(Instant.now())
                .build();
    }

    private OrderResponse stubOrderResponse(Order o) {
        return new OrderResponse(o.getId(), o.getTitle(), null, null, null, null,
                o.getStatus(), o.getSource(), null, null, o.getCreatedAt(), null, List.of());
    }

    @Test
    void create_savesOrderAndRecordsInitialHistory() {
        CreateOrderRequest req = new CreateOrderRequest("AirPods", null, "Apple", null, null, null, null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(stubOrderResponse(order));
        when(historyRepository.findAllByOrderOrderByChangedAtAsc(order)).thenReturn(List.of());

        OrderResponse result = orderService.create("test@example.com", req);

        assertThat(result.status()).isEqualTo(OrderStatus.PENDING);
        verify(historyRepository).save(any(OrderStatusHistory.class));
    }

    @Test
    void changeStatus_appendsHistoryEntry() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUser(order.getId(), user)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any())).thenReturn(stubOrderResponse(order));
        when(historyRepository.findAllByOrderOrderByChangedAtAsc(any())).thenReturn(List.of());

        ChangeOrderStatusRequest req = new ChangeOrderStatusRequest(OrderStatus.SHIPPED, "Dispatched");
        orderService.changeStatus("test@example.com", order.getId(), req);

        verify(historyRepository).save(argThat(h ->
                h.getNewStatus() == OrderStatus.SHIPPED &&
                h.getOldStatus() == OrderStatus.PENDING));
    }

    @Test
    void changeStatus_noOpWhenStatusUnchanged() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUser(order.getId(), user)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(stubOrderResponse(order));
        when(historyRepository.findAllByOrderOrderByChangedAtAsc(order)).thenReturn(List.of());

        ChangeOrderStatusRequest req = new ChangeOrderStatusRequest(OrderStatus.PENDING, null);
        orderService.changeStatus("test@example.com", order.getId(), req);

        verify(historyRepository, never()).save(any());
    }

    @Test
    void list_filtersByStatus() {
        Order shipped = Order.builder().id(UUID.randomUUID()).user(user).title("Shipped")
                .status(OrderStatus.SHIPPED).source(OrderSource.MANUAL).createdAt(Instant.now()).build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findAllByUserAndStatus(user, OrderStatus.SHIPPED)).thenReturn(List.of(shipped));
        when(orderMapper.toDto(shipped)).thenReturn(stubOrderResponse(shipped));

        List<OrderResponse> result = orderService.list("test@example.com", OrderStatus.SHIPPED, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void get_throwsNotFoundForDifferentUser() {
        User other = User.builder().id(UUID.randomUUID()).email("other@example.com").build();
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(other));
        when(orderRepository.findByIdAndUser(order.getId(), other)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.get("other@example.com", order.getId()))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void delete_removesOrder() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUser(order.getId(), user)).thenReturn(Optional.of(order));

        orderService.delete("test@example.com", order.getId());

        verify(orderRepository).delete(order);
    }

    @Test
    void update_changesStatusAndRecordsHistory() {
        UpdateOrderRequest req = new UpdateOrderRequest("AirPods Pro", null, "Apple", null, null,
                OrderStatus.CONFIRMED, null, null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(orderRepository.findByIdAndUser(order.getId(), user)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toDto(any())).thenReturn(stubOrderResponse(order));
        when(historyRepository.findAllByOrderOrderByChangedAtAsc(any())).thenReturn(List.of());

        orderService.update("test@example.com", order.getId(), req);

        verify(historyRepository).save(argThat(h -> h.getNewStatus() == OrderStatus.CONFIRMED));
    }
}
