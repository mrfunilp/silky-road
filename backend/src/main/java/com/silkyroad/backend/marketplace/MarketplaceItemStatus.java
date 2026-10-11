package com.silkyroad.backend.marketplace;

import java.util.Locale;

public enum MarketplaceItemStatus {
    DRAFT,
    ACTIVE,
    SOLD,
    ARCHIVED;

    public String toValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static MarketplaceItemStatus fromValue(String value) {
        if (value == null || value.isBlank()) {
            return DRAFT;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Unknown status '" + value + "'. Expected one of: draft, active, sold, archived.");
        }
    }
}
