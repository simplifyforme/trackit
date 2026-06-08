package com.example.template.order.mapper;

import com.example.template.order.dto.OrderResponse;
import com.example.template.order.dto.OrderStatusHistoryResponse;
import com.example.template.order.entity.Order;
import com.example.template.order.entity.OrderStatusHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface OrderMapper {

    @Mapping(target = "statusHistory", ignore = true)
    OrderResponse toDto(Order order);

    OrderStatusHistoryResponse toHistoryDto(OrderStatusHistory history);
}
