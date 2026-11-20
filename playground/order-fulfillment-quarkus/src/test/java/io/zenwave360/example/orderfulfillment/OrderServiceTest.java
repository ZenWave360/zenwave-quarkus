package io.zenwave360.example.orderfulfillment;

import static org.junit.jupiter.api.Assertions.*;

import io.zenwave360.example.orderfulfillment.config.*;
import io.zenwave360.example.orderfulfillment.domain.*;
import io.zenwave360.example.orderfulfillment.domain.OrderTransitions.InvalidStateTransitionException;
import io.zenwave360.example.orderfulfillment.dtos.*;
import io.zenwave360.example.orderfulfillment.events.InMemoryOrderEventsProducer;
import io.zenwave360.example.orderfulfillment.inmemory.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Acceptance Test for OrderService (in-memory, no container).
 */
public class OrderServiceTest {

    static final String ORDER_NUMBER = "ORD-0001"; // src/test/resources/data/jpa/Order/1.json

    final ServicesInMemoryConfig context = new ServicesInMemoryConfig();
    final OrderService orderService = context.orderService();
    final OrderRepositoryInMemory orderRepository = context.orderRepository();
    final InMemoryOrderEventsProducer events = context.eventsProducerInMemoryContext().orderEventsProducer();

    @BeforeEach
    void setUp() {
        context.reloadTestData();
        events.getCapturedMessages().clear();
    }

    @Test
    void placeOrderTest() {
        var input = new PlaceOrderInput()
                .setCurrency("EUR")
                .setItems(List.of(
                        new OrderItem().setProductId("P-1").setProductName("Desk").setQuantity(2).setUnitPrice(new BigDecimal("100.00")),
                        new OrderItem().setProductId("P-2").setProductName("Lamp").setQuantity(1).setUnitPrice(new BigDecimal("25.50"))));

        var order = orderService.placeOrder(input);

        assertNotNull(order.getId());
        assertNotNull(order.getOrderNumber());
        assertEquals(OrderStatus.PLACED, order.getStatus());
        assertEquals(new BigDecimal("225.50"), order.getTotalAmount());
        assertTrue(orderRepository.findByOrderNumber(order.getOrderNumber()).isPresent());
        assertEquals(1, events.getOnOrderPlacedCapturedMessages().size());
    }

    @Test
    void payOrderTest() {
        var order = orderService.payOrder(ORDER_NUMBER, new PayOrderInput().setPaymentReference("PAY-1"));

        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals("PAY-1", order.getPaymentReference());
        assertEquals(1, events.getOnOrderPaidCapturedMessages().size());
    }

    @Test
    void shipOrderTest() {
        orderService.payOrder(ORDER_NUMBER, new PayOrderInput().setPaymentReference("PAY-1"));

        var order = orderService.shipOrder(ORDER_NUMBER, new ShipOrderInput().setTrackingNumber("TRK-1"));

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        assertEquals("TRK-1", order.getTrackingNumber());
        assertEquals(1, events.getOnOrderShippedCapturedMessages().size());
    }

    @Test
    void shipUnpaidOrderIsRejected() {
        assertThrows(InvalidStateTransitionException.class,
                () -> orderService.shipOrder(ORDER_NUMBER, new ShipOrderInput().setTrackingNumber("TRK-1")));
        assertTrue(events.getOnOrderShippedCapturedMessages().isEmpty());
    }

    @Test
    void cancelOrderTest() {
        var order = orderService.cancelOrder(ORDER_NUMBER);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertEquals(1, events.getOnOrderCancelledCapturedMessages().size());
    }

    @Test
    void cancelUnknownOrderFails() {
        assertThrows(NoSuchElementException.class, () -> orderService.cancelOrder("missing"));
    }

    @Test
    void getOrderTest() {
        var order = orderService.getOrder(ORDER_NUMBER);
        assertNotNull(order);
        assertEquals(2, order.getItems().size());
        assertNull(orderService.getOrder("missing"));
    }
}
