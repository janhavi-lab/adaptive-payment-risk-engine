package com.janhavi.apre.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentMethod {

    CREDIT_CARD,
    DEBIT_CARD,
    UPI,
    NET_BANKING,
    WALLET;

    @JsonCreator
    public static PaymentMethod fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return UPI;
        }
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (PaymentMethod method : values()) {
            if (method.name().equalsIgnoreCase(normalized)) {
                return method;
            }
        }
        return UPI;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}