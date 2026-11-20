package io.zenwave360.example.orderfulfillment.mappers;

import io.zenwave360.example.orderfulfillment.domain.*;
import io.zenwave360.example.orderfulfillment.dtos.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi", uses = BaseMapper.class)
public interface OrderServiceMapper {

    // input mappings
    // PayOrderInputnull-Order payOrder

    Order asOrder(PayOrderInput input);

    @Mapping(target = "id", ignore = true)
    Order update(@MappingTarget Order entity, PayOrderInput input);
    // ShipOrderInputnull-Order shipOrder

    Order asOrder(ShipOrderInput input);

    @Mapping(target = "id", ignore = true)
    Order update(@MappingTarget Order entity, ShipOrderInput input);
    // PlaceOrderInputnull-Order placeOrder

    Order asOrder(PlaceOrderInput input);

    @Mapping(target = "id", ignore = true)
    Order update(@MappingTarget Order entity, PlaceOrderInput input);
    // output mappings
}
