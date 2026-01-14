package com.luggagestorage.model.enums;

public enum Size {
    SMALL("Small", "Suitable for backpacks and small bags"),
    MEDIUM("Medium", "Suitable for carry-on luggage"),
    LARGE("Large", "Suitable for large suitcases");

    private final String displayName;
    private final String description;

    Size(String displayName, String description) {
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
