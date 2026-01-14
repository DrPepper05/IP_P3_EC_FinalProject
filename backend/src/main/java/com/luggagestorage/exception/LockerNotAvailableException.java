package com.luggagestorage.exception;

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
