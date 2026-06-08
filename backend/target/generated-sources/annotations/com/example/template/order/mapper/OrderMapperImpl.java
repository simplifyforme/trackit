package com.example.template.order.mapper;

import com.example.template.order.dto.OrderResponse;
import com.example.template.order.dto.OrderStatusHistoryResponse;
import com.example.template.order.entity.Order;
import com.example.template.order.entity.OrderSource;
import com.example.template.order.entity.OrderStatus;
import com.example.template.order.entity.OrderStatusHistory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-07T17:33:59+0200",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Override
    public OrderResponse toDto(Order order) {
        if ( order == null ) {
            return null;
        }

        UUID id = null;
        String title = null;
        String description = null;
        String merchant = null;
        BigDecimal amount = null;
        String currency = null;
        OrderStatus status = null;
        OrderSource source = null;
        String externalRef = null;
        Instant orderDate = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        id = order.getId();
        title = order.getTitle();
        description = order.getDescription();
        merchant = order.getMerchant();
        amount = order.getAmount();
        currency = order.getCurrency();
        status = order.getStatus();
        source = order.getSource();
        externalRef = order.getExternalRef();
        orderDate = order.getOrderDate();
        createdAt = order.getCreatedAt();
        updatedAt = order.getUpdatedAt();

        List<OrderStatusHistoryResponse> statusHistory = null;

        OrderResponse orderResponse = new OrderResponse( id, title, description, merchant, amount, currency, status, source, externalRef, orderDate, createdAt, updatedAt, statusHistory );

        return orderResponse;
    }

    @Override
    public OrderStatusHistoryResponse toHistoryDto(OrderStatusHistory history) {
        if ( history == null ) {
            return null;
        }

        UUID id = null;
        OrderStatus oldStatus = null;
        OrderStatus newStatus = null;
        Instant changedAt = null;
        OrderSource source = null;
        String note = null;

        id = history.getId();
        oldStatus = history.getOldStatus();
        newStatus = history.getNewStatus();
        changedAt = history.getChangedAt();
        source = history.getSource();
        note = history.getNote();

        OrderStatusHistoryResponse orderStatusHistoryResponse = new OrderStatusHistoryResponse( id, oldStatus, newStatus, changedAt, source, note );

        return orderStatusHistoryResponse;
    }
}
