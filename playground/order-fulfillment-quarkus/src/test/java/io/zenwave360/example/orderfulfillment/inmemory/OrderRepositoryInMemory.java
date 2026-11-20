package io.zenwave360.example.orderfulfillment.inmemory;

import io.zenwave360.example.orderfulfillment.OrderRepository;
import io.zenwave360.example.orderfulfillment.domain.*;
import java.util.Objects;

public class OrderRepositoryInMemory extends InMemoryJpaRepository<Order, Long> implements OrderRepository {
    @Override
    public java.util.Optional<Order> findByOrderNumber(String orderNumber) {
        return java.util.Optional.ofNullable(entities.values().stream()
                .filter(entity -> Objects.equals(entity.getOrderNumber(), orderNumber))
                .findFirst()
                .orElse(null));
    }

    private long sequence = 1;

    @Override
    protected Long idOf(Order entity) {
        return entity.getId();
    }

    @Override
    protected void setId(Order entity, Long id) {
        entity.setId(id);
    }

    @Override
    protected Long newId() {
        long next = sequence++;

        return next;
    }
}
