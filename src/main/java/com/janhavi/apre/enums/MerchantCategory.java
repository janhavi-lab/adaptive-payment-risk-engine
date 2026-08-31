package com.janhavi.apre.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MerchantCategory {

    ECOMMERCE,
    TRAVEL,
    GROCERY,
    FOOD,
    ENTERTAINMENT,
    HEALTHCARE,
    EDUCATION,
    OTHER;

    @JsonCreator
    public static MerchantCategory fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ECOMMERCE;
        }
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (MerchantCategory category : values()) {
            if (category.name().equalsIgnoreCase(normalized)) {
                return category;
            }
        }
        return OTHER;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}