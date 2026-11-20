package io.zenwave360.example.orderfulfillment.config;

import io.zenwave360.example.orderfulfillment.*;
import io.zenwave360.example.orderfulfillment.domain.*;
import io.zenwave360.example.orderfulfillment.events.*;
import io.zenwave360.example.orderfulfillment.mappers.*;
import java.util.List;
import org.mapstruct.factory.Mappers;

/**
 * In-memory services. Constructed the same way the CDI beans are, without a container.
 */
public class ServicesInMemoryConfig extends RepositoriesInMemoryConfig {

    protected final EventsProducerInMemoryContext eventsProducerInMemoryContext = new EventsProducerInMemoryContext();

    public EventsProducerInMemoryContext eventsProducerInMemoryContext() {
        return eventsProducerInMemoryContext;
    }

    protected final OrderService orderService = new OrderServiceImpl(
            orderRepository(),
            Mappers.getMapper(OrderServiceMapper.class),
            eventsProducerInMemoryContext.orderEventsProducer(),
            Mappers.getMapper(EventsMapper.class));

    public OrderService orderService() {
        return orderService;
    }

    static List<Order> _orders;

    public void reloadTestData() {

        var testDataLoader = new TestDataLoader(List.of(Order.class, OrderItem.class));
        var orders = _orders != null ? _orders : testDataLoader.loadCollectionTestDataAsObjects(Order.class);
        orderRepository().deleteAll();
        orderRepository().saveAll(orders);
    }
}
