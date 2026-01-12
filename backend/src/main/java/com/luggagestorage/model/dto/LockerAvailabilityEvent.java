package com.luggagestorage.model.dto;

import com.luggagestorage.model.enums.Status;

import java.time.LocalDateTime;

/**
 * DTO for WebSocket locker availability events.
 * Broadcasts real-time updates when locker status changes.
 */
public class LockerAvailabilityEvent {

    private Long lockerId;
    private String lockerNumber;
    private Status status;
    private String size;
    private LocalDateTime timestamp;
    private String message;

    public LockerAvailabilityEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public LockerAvailabilityEvent(Long lockerId, String lockerNumber, Status status, String size, String message) {
        this.lockerId = lockerId;
        this.lockerNumber = lockerNumber;
        this.status = status;
        this.size = size;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getLockerId() {
        return lockerId;
    }

    public void setLockerId(Long lockerId) {
        this.lockerId = lockerId;
    }

    public String getLockerNumber() {
        return lockerNumber;
    }

    public void setLockerNumber(String lockerNumber) {
        this.lockerNumber = lockerNumber;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "LockerAvailabilityEvent{" +
                "lockerId=" + lockerId +
                ", lockerNumber='" + lockerNumber + '\'' +
                ", status=" + status +
                ", size='" + size + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
}
