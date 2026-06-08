package com.example.template.order.repository;

import com.example.template.order.entity.Order;
import com.example.template.order.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {

    List<OrderStatusHistory> findAllByOrderOrderByChangedAtAsc(Order order);
}
