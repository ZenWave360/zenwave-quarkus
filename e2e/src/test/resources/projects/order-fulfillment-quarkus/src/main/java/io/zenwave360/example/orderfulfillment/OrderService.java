package io.zenwave360.example.orderfulfillment;

import io.zenwave360.example.orderfulfillment.domain.*;
import io.zenwave360.example.orderfulfillment.dtos.*;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * Inbound service port for managing [Order].
 */
public interface OrderService {

    /**
     * Customer places an order
     *
     * With Events: [OrderPlaced].
     *
     */
    public Order placeOrder(PlaceOrderInput input);

    /**
     * Set Order as paid
     *
     * With Events: [OrderPaid].
     *
     */
    public Order payOrder(String orderNumber, PayOrderInput input);

    /**
     * Set Order as shipped
     *
     * With Events: [OrderShipped].
     *
     */
    public Order shipOrder(String orderNumber, ShipOrderInput input);

    /**
     * Set Order as cancelled
     *
     * With Events: [OrderCancelled].
     *
     */
    public Order cancelOrder(String orderNumber);

    /**
     * Query order
     *
     */
    public Order getOrder(String orderNumber);
}
