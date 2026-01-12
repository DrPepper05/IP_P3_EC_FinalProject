package com.luggagestorage.exception;

/**
 * Custom exception thrown when a locker is not available for booking.
 * Part of the optional requirement: "Create at least 2 custom exceptions" (1 point).
 */
public class LockerNotAvailableException extends RuntimeException {

    private Long lockerId;

    public LockerNotAvailableException(String message) {
        super(message);
    }

    public LockerNotAvailableException(String message, Long lockerId) {
        super(message);
        this.lockerId = lockerId;
    }

    public LockerNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public Long getLockerId() {
        return lockerId;
    }

    public void setLockerId(Long lockerId) {
        this.lockerId = lockerId;
    }
}
