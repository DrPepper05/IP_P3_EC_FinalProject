package com.luggagestorage.model.enums;

/**
 * Enum representing user roles for authorization.
 * Supports role-based access control (RBAC).
 */
public enum Role {
    CUSTOMER("Customer", "Regular user who can make bookings"),
    ADMIN("Admin", "Administrator with full system access");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get Spring Security role name (with ROLE_ prefix)
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
