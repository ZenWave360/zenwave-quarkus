package io.zenwave360.example.orderfulfillment.config;

import io.zenwave360.example.orderfulfillment.*;
import io.zenwave360.example.orderfulfillment.inmemory.*;

public class RepositoriesInMemoryConfig {

    protected final OrderRepositoryInMemory orderRepository = new OrderRepositoryInMemory();

    public OrderRepositoryInMemory orderRepository() {
        return orderRepository;
    }
}
