package io.zenwave360.example.orderfulfillment;

import io.zenwave360.example.orderfulfillment.domain.*;
import io.zenwave360.example.orderfulfillment.dtos.*;
import io.zenwave360.example.orderfulfillment.events.*;
import io.zenwave360.example.orderfulfillment.mappers.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service implementation for managing [Order].
 */
@ApplicationScoped
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final OrderServiceMapper orderServiceMapper;

    private final OrderEventsProducer eventsProducer;
    private final EventsMapper eventsMapper;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    public OrderServiceImpl(
            OrderRepository orderRepository,
            OrderServiceMapper orderServiceMapper,
            OrderEventsProducer eventsProducer,
            EventsMapper eventsMapper) {

        this.orderRepository = orderRepository;

        this.orderServiceMapper = orderServiceMapper;

        this.eventsProducer = eventsProducer;
        this.eventsMapper = eventsMapper;
    }

    @Override
    public Order placeOrder(PlaceOrderInput input) {

        log.debug("Request placeOrder: {}", input);

        var order = orderServiceMapper.update(new Order(), input);
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setStatus(OrderStatus.PLACED);
        order.setTotalAmount(order.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        order = orderRepository.save(order);
        // emit events
        eventsProducer.onOrderPlaced(eventsMapper.asOrderPlaced(order));
        return order;
    }

    @Override
    public Order payOrder(String orderNumber, PayOrderInput input) {

        log.debug("Request payOrder: {} {}", orderNumber, input);

        var existingOrder = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        if (existingOrder == null) {
            throw new java.util.NoSuchElementException("Order not found with id: " + orderNumber);
        }
        OrderTransitions.ensureCanPayOrder(existingOrder);
        orderServiceMapper.update(existingOrder, input);
        existingOrder.setStatus(OrderStatus.PAID);
        var saved = orderRepository.save(existingOrder);
        // emit events
        eventsProducer.onOrderPaid(eventsMapper.asOrderPaid(saved));
        return saved;
    }

    @Override
    public Order shipOrder(String orderNumber, ShipOrderInput input) {

        log.debug("Request shipOrder: {} {}", orderNumber, input);

        var existingOrder = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        if (existingOrder == null) {
            throw new java.util.NoSuchElementException("Order not found with id: " + orderNumber);
        }
        OrderTransitions.ensureCanShipOrder(existingOrder);
        orderServiceMapper.update(existingOrder, input);
        existingOrder.setStatus(OrderStatus.SHIPPED);
        var saved = orderRepository.save(existingOrder);
        // emit events
        eventsProducer.onOrderShipped(eventsMapper.asOrderShipped(saved));
        return saved;
    }

    @Override
    public Order cancelOrder(String orderNumber) {

        log.debug("Request cancelOrder: {}", orderNumber);

        var existingOrder = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        if (existingOrder == null) {
            throw new java.util.NoSuchElementException("Order not found with id: " + orderNumber);
        }
        OrderTransitions.ensureCanCancelOrder(existingOrder);
        existingOrder.setStatus(OrderStatus.CANCELLED);
        var order = orderRepository.save(existingOrder);
        // emit events
        eventsProducer.onOrderCancelled(eventsMapper.asOrderCancelled(order));
        return order;
    }

    @Override
    public Order getOrder(String orderNumber) {

        log.debug("Request getOrder: {}", orderNumber);
        var order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        return order;
    }
}
