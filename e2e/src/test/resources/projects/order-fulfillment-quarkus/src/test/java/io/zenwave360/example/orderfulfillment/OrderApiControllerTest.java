package io.zenwave360.example.orderfulfillment;

import static org.junit.jupiter.api.Assertions.*;

import io.zenwave360.example.orderfulfillment.config.ServicesInMemoryConfig;
import io.zenwave360.example.orderfulfillment.mappers.*;
import io.zenwave360.example.orderfulfillment.web.model.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

/**
 * OrderApiController with in-memory services (no container).
 */
public class OrderApiControllerTest {

    static final String ORDER_NUMBER = "ORD-0001";

    private final ServicesInMemoryConfig context = new ServicesInMemoryConfig();

    private final OrderApiController controller =
            new OrderApiController(context.orderService(), Mappers.getMapper(OrderDTOsMapper.class));

    @BeforeEach
    void setUp() {
        context.reloadTestData();
    }

    @Test
    void placeOrderTest() {
        var reqBody = new PlaceOrderInputDTO()
                .currency("EUR")
                .items(List.of(new OrderItemDTO().productId("P-1").productName("Desk").quantity(1).unitPrice(new BigDecimal("10.00"))));
        var response = controller.placeOrder(reqBody);
        assertEquals(201, response.getStatus());
        var order = (OrderDTO) response.getEntity();
        assertNotNull(order.getOrderNumber());
        assertEquals(OrderStatusDTO.PLACED, order.getStatus());
    }

    @Test
    void payOrderTest() {
        var response = controller.payOrder(ORDER_NUMBER, new PayOrderInputDTO().paymentReference("PAY-1"));
        assertEquals(201, response.getStatus());
        assertEquals(OrderStatusDTO.PAID, ((OrderDTO) response.getEntity()).getStatus());
    }

    @Test
    void shipOrderTest() {
        controller.payOrder(ORDER_NUMBER, new PayOrderInputDTO().paymentReference("PAY-1"));
        var response = controller.shipOrder(ORDER_NUMBER, new ShipOrderInputDTO().trackingNumber("TRK-1"));
        assertEquals(201, response.getStatus());
        assertEquals(OrderStatusDTO.SHIPPED, ((OrderDTO) response.getEntity()).getStatus());
    }

    @Test
    void cancelOrderTest() {
        var response = controller.cancelOrder(ORDER_NUMBER);
        assertEquals(201, response.getStatus());
        assertEquals(OrderStatusDTO.CANCELLED, ((OrderDTO) response.getEntity()).getStatus());
    }

    @Test
    void getOrderTest() {
        assertEquals(200, controller.getOrder(ORDER_NUMBER).getStatus());
        assertEquals(404, controller.getOrder("missing").getStatus());
    }
}
