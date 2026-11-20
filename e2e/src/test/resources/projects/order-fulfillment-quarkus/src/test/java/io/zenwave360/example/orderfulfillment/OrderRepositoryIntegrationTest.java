package io.zenwave360.example.orderfulfillment;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.zenwave360.example.orderfulfillment.domain.*;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestTransaction
public class OrderRepositoryIntegrationTest extends BaseRepositoryIntegrationTest {

    @Inject
    EntityManager entityManager;

    @Inject
    OrderRepository orderRepository;

    @Test
    void findAllTest() {
        var results = orderRepository.findAll();
        Assertions.assertFalse(results.isEmpty());
    }

    @Test
    void findByIdTest() {
        var id = 1L;
        var order = orderRepository.findById(id);
        if (order == null) {
            throw new java.util.NoSuchElementException("Order not found with id: " + id);
        }
        Assertions.assertNotNull(order.getId());
        Assertions.assertNotNull(order.getVersion());
        Assertions.assertNotNull(order.getCreatedBy());
        Assertions.assertNotNull(order.getCreatedDate());
    }

    @Test
    void saveTest() {
        var order = new Order();
        order.setOrderNumber("");
        order.setStatus(OrderStatus.values()[0]);
        order.setTotalAmount(BigDecimal.valueOf(0));
        order.setCurrency("");
        order.setPaymentReference("");
        order.setTrackingNumber("");
        order.setItems(List.of(new OrderItem()));

        var created = orderRepository.save(order);

        entityManager.flush();
        entityManager.refresh(created);
        Assertions.assertNotNull(created.getId());
        Assertions.assertNotNull(created.getVersion());
        Assertions.assertNotNull(created.getCreatedBy());
        Assertions.assertNotNull(created.getCreatedDate());
    }

    @Test
    void updateTest() {
        var id = 1L;
        var order = orderRepository.findById(id);
        if (order == null) {
            throw new java.util.NoSuchElementException("Order not found with id: " + id);
        }
        order.setOrderNumber("");
        order.setStatus(OrderStatus.values()[0]);
        order.setTotalAmount(BigDecimal.valueOf(0));
        order.setCurrency("");
        order.setPaymentReference("");
        order.setTrackingNumber("");
        order.setItems(List.of(new OrderItem()));

        var updated = orderRepository.save(order);
        Assertions.assertEquals("", updated.getOrderNumber());
        Assertions.assertEquals(OrderStatus.values()[0], updated.getStatus());
        Assertions.assertEquals(BigDecimal.valueOf(0), updated.getTotalAmount());
        Assertions.assertEquals("", updated.getCurrency());
        Assertions.assertEquals("", updated.getPaymentReference());
        Assertions.assertEquals("", updated.getTrackingNumber());
        Assertions.assertEquals(List.of(new OrderItem()), updated.getItems());
    }

    @Test
    void deleteTest() {
        var id = 1L;
        orderRepository.deleteById(id);
        var notFound = orderRepository.findById(id);
        Assertions.assertNull(notFound);
    }
}
