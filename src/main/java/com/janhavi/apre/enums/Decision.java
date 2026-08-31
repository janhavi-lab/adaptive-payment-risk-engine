package com.janhavi.apre.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Decision {

    APPROVED,
    APPROVED_WITH_MONITORING,
    MANUAL_REVIEW,
    DECLINED;

    @JsonCreator
    public static Decision fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return APPROVED;
        }
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (Decision decision : values()) {
            if (decision.name().equalsIgnoreCase(normalized)) {
                return decision;
            }
        }
        return APPROVED;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}