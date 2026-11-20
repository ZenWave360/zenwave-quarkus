package io.zenwave360.example.orderfulfillment.mappers;

import io.zenwave360.example.orderfulfillment.domain.*;
import io.zenwave360.example.orderfulfillment.dtos.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi", uses = BaseMapper.class)
public interface EventsMapper {
    io.zenwave360.example.orderfulfillment.events.dtos.OrderPaid asOrderPaid(Order order);

    io.zenwave360.example.orderfulfillment.events.dtos.OrderStatus asOrderStatus(OrderStatus orderStatus);

    io.zenwave360.example.orderfulfillment.events.dtos.OrderPlaced asOrderPlaced(Order order);

    io.zenwave360.example.orderfulfillment.events.dtos.OrderShipped asOrderShipped(Order order);

    io.zenwave360.example.orderfulfillment.events.dtos.OrderCancelled asOrderCancelled(Order order);

    io.zenwave360.example.orderfulfillment.events.dtos.OrderItem asOrderItem(OrderItem orderItem);
}
