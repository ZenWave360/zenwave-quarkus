package io.zenwave360.example.orderfulfillment;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.zenwave360.example.orderfulfillment.config.TestAuditorProvider;
import io.zenwave360.example.orderfulfillment.domain.Order;
import io.zenwave360.example.orderfulfillment.domain.OrderItem;
import io.zenwave360.example.orderfulfillment.domain.OrderStatus;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestTransaction
class AuditingTest {

    @Inject
    OrderRepository orderRepository;

    @Inject
    EntityManager entityManager;

    @Test
    void auditorProviderBeanFillsCreatedAndModifiedBy() {
        var order = orderRepository.save(new Order()
                .setOrderNumber(UUID.randomUUID().toString())
                .setStatus(OrderStatus.PLACED)
                .setTotalAmount(BigDecimal.TEN)
                .setCurrency("EUR")
                .setItems(List.of(new OrderItem()
                        .setProductId("P-1")
                        .setProductName("Book")
                        .setQuantity(1)
                        .setUnitPrice(BigDecimal.TEN))));
        entityManager.flush();

        Assertions.assertEquals(TestAuditorProvider.AUDITOR, order.getCreatedBy());
        Assertions.assertEquals(TestAuditorProvider.AUDITOR, order.getLastModifiedBy());
        Assertions.assertNotNull(order.getCreatedDate());
    }
}
