package com.luggagestorage.model.enums;

/**
 * Enum representing booking status lifecycle.
 * Part of the minimum 2 interfaces/enums requirement.
 */
public enum BookingStatus {
    ACTIVE("Active", "Booking is currently active"),
    COMPLETED("Completed", "Booking has been completed"),
    CANCELLED("Cancelled", "Booking was cancelled");

    private final String displayName;
    private final String description;

    BookingStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
