package com.luggagestorage.model.dto;

import java.time.LocalDateTime;

public class BookingEvent {

    private Long bookingId;
    private Long lockerId;
    private String lockerNumber;
    private String eventType;
    private String customerName;
    private LocalDateTime timestamp;
    private String message;

    public BookingEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public BookingEvent(Long bookingId, Long lockerId, String lockerNumber, String eventType, String customerName, String message) {
        this.bookingId = bookingId;
        this.lockerId = lockerId;
        this.lockerNumber = lockerNumber;
        this.eventType = eventType;
        this.customerName = customerName;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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
        return "BookingEvent{" +
                "bookingId=" + bookingId +
                ", lockerId=" + lockerId +
                ", lockerNumber='" + lockerNumber + '\'' +
                ", eventType='" + eventType + '\'' +
                ", customerName='" + customerName + '\'' +
                ", timestamp=" + timestamp +
                ", message='" + message + '\'' +
                '}';
    }
}
