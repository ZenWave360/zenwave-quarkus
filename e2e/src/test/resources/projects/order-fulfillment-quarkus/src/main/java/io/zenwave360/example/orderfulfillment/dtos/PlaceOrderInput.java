package io.zenwave360.example.orderfulfillment.dtos;

import io.zenwave360.example.orderfulfillment.domain.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * PlaceOrderInput.
 */
public class PlaceOrderInput implements Serializable {

    @Size(min = 1)
    private List<OrderItem> items = new ArrayList<>();

    @NotNull
    @Size(max = 3)
    private String currency;

    public PlaceOrderInput addItems(OrderItem items) {
        this.items.add(items);
        return this;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public PlaceOrderInput setItems(List<OrderItem> items) {
        this.items = items;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public PlaceOrderInput setCurrency(String currency) {
        this.currency = currency;
        return this;
    }
}
