package io.zenwave360.example.orderfulfillment;

import io.zenwave360.example.orderfulfillment.dtos.*;
import io.zenwave360.example.orderfulfillment.mappers.*;
import io.zenwave360.example.orderfulfillment.web.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAX-RS resource for Order.
 */
@ApplicationScoped
@Path("/")
public class OrderApiController {

    private final OrderService orderService;

    private final OrderDTOsMapper mapper;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Inject
    public OrderApiController(OrderService orderService, OrderDTOsMapper mapper) {

        this.orderService = orderService;

        this.mapper = mapper;
    }

    @POST
    @Path("/orders")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response placeOrder(@Valid PlaceOrderInputDTO reqBody) {
        log.debug("REST request to placeOrder: {}", reqBody);
        var input = mapper.asPlaceOrderInput(reqBody);
        var order = orderService.placeOrder(input);
        var responseDTO = mapper.asOrderDTO(order);
        return Response.status(201).entity(responseDTO).build();
    }

    @POST
    @Path("/orders/{orderNumber}/pay")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response payOrder(@PathParam("orderNumber") String orderNumber, @Valid PayOrderInputDTO reqBody) {
        log.debug("REST request to payOrder: {}, {}", orderNumber, reqBody);
        var input = mapper.asPayOrderInput(reqBody);
        var order = orderService.payOrder(orderNumber, input);
        var responseDTO = mapper.asOrderDTO(order);
        return Response.status(201).entity(responseDTO).build();
    }

    @POST
    @Path("/orders/{orderNumber}/ship")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response shipOrder(@PathParam("orderNumber") String orderNumber, @Valid ShipOrderInputDTO reqBody) {
        log.debug("REST request to shipOrder: {}, {}", orderNumber, reqBody);
        var input = mapper.asShipOrderInput(reqBody);
        var order = orderService.shipOrder(orderNumber, input);
        var responseDTO = mapper.asOrderDTO(order);
        return Response.status(201).entity(responseDTO).build();
    }

    @POST
    @Path("/orders/{orderNumber}/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancelOrder(@PathParam("orderNumber") String orderNumber) {
        log.debug("REST request to cancelOrder: {}", orderNumber);
        var order = orderService.cancelOrder(orderNumber);
        var responseDTO = mapper.asOrderDTO(order);
        return Response.status(201).entity(responseDTO).build();
    }

    @GET
    @Path("/orders/{orderNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrder(@PathParam("orderNumber") String orderNumber) {
        log.debug("REST request to getOrder: {}", orderNumber);
        var order = orderService.getOrder(orderNumber);
        if (order != null) {
            return Response.status(200).entity(mapper.asOrderDTO(order)).build();
        }
        return Response.status(404).build();
    }
}
