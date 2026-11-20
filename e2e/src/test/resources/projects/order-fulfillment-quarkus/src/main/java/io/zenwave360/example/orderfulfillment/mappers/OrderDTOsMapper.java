package io.zenwave360.example.orderfulfillment.mappers;

import io.zenwave360.example.orderfulfillment.domain.*;
import io.zenwave360.example.orderfulfillment.dtos.*;
import io.zenwave360.example.orderfulfillment.web.model.*;
import java.math.*;
import java.time.*;
import java.util.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi", uses = BaseMapper.class)
public interface OrderDTOsMapper {

    // request mappings
    PlaceOrderInput asPlaceOrderInput(PlaceOrderInputDTO dto);

    ShipOrderInput asShipOrderInput(ShipOrderInputDTO dto);

    PayOrderInput asPayOrderInput(PayOrderInputDTO dto);

    // response mappings

    OrderDTO asOrderDTO(Order entity);
}
