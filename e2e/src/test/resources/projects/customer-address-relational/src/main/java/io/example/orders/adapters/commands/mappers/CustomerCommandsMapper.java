package io.example.orders.adapters.commands.mappers;

import io.example.orders.core.inbound.dtos.AddressInput;
import io.example.orders.core.inbound.dtos.CustomerInput;
import org.mapstruct.Mapper;

/** AsyncAPI command DTOs to core inbound DTOs. */
@Mapper(componentModel = "cdi")
public interface CustomerCommandsMapper {

    CustomerInput asCustomerInput(io.example.orders.core.outbound.events.dtos.CustomerInput command);

    AddressInput asAddressInput(io.example.orders.core.outbound.events.dtos.AddressInput command);
}
