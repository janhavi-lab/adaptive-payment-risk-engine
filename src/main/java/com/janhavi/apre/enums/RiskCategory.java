package com.janhavi.apre.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RiskCategory {

    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    @JsonCreator
    public static RiskCategory fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LOW;
        }
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (RiskCategory category : values()) {
            if (category.name().equalsIgnoreCase(normalized)) {
                return category;
            }
        }
        return LOW;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}