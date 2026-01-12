package com.luggagestorage.model.enums;

/**
 * Enum representing locker availability status.
 * Part of the minimum 2 interfaces/enums requirement.
 */
public enum Status {
    AVAILABLE("Available", "Locker is ready to be booked"),
    OCCUPIED("Occupied", "Locker is currently in use"),
    RESERVED("Reserved", "Locker is reserved for upcoming booking"),
    MAINTENANCE("Maintenance", "Locker is under maintenance"),
    OUT_OF_ORDER("Out of Order", "Locker is broken/unavailable");

    private final String displayName;
    private final String description;

    Status(String displayName, String description) {
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
