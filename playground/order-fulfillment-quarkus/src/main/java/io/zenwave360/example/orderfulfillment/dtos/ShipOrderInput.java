package io.zenwave360.example.orderfulfillment.dtos;

import io.zenwave360.example.orderfulfillment.domain.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * ShipOrderInput.
 */
public class ShipOrderInput implements Serializable {

    @NotNull
    private String trackingNumber;

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public ShipOrderInput setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
        return this;
    }
}
