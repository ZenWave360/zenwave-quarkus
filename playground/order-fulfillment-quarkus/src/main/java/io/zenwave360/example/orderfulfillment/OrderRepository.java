package io.zenwave360.example.orderfulfillment;

import io.zenwave360.example.orderfulfillment.domain.*;
import java.util.List;

/**
 * Outbound repository port for the Order entity.
 */
public interface OrderRepository {

    Order save(Order entity);

    List<Order> saveAll(List<Order> entities);

    Order findById(Long id);

    void deleteById(Long id);

    void delete(Order entity);

    List<Order> findAll();

    List<Order> findAll(int page, int size);

    java.util.Optional<Order> findByOrderNumber(String orderNumber);
}
