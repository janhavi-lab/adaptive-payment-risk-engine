package com.janhavi.apre.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {

    ADMIN,
    ANALYST;

    @JsonCreator
    public static Role fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ANALYST;
        }
        String normalized = value.trim().toUpperCase().replace("-", "_").replace(" ", "_");
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(normalized)) {
                return role;
            }
        }
        return ANALYST;
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}