package io.example.orders.adapters.commands;

import io.example.orders.adapters.commands.mappers.CustomerCommandsMapper;
import io.example.orders.core.inbound.CustomerService;
import io.example.orders.core.outbound.events.dtos.CustomerInput;
import jakarta.enterprise.context.ApplicationScoped;

/** Hand-written: customerCommandsChannel commands to the CustomerService use cases. */
@ApplicationScoped
public class CustomerCommandsService implements ICustomerCommandsChannelConsumerService {

    private final CustomerService customerService;
    private final CustomerCommandsMapper mapper;

    public CustomerCommandsService(CustomerService customerService, CustomerCommandsMapper mapper) {
        this.customerService = customerService;
        this.mapper = mapper;
    }

    @Override
    public void createCustomer(CustomerInput payload, CustomerInputHeaders headers) {
        customerService.createCustomer(mapper.asCustomerInput(payload));
    }
}
