package io.zenwave360.example.orderfulfillment.dtos;

import io.zenwave360.example.orderfulfillment.domain.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.*;
import java.time.*;
import java.util.*;

/**
 * PayOrderInput.
 */
public class PayOrderInput implements Serializable {

    @NotNull
    private String paymentReference;

    public String getPaymentReference() {
        return paymentReference;
    }

    public PayOrderInput setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
        return this;
    }
}
