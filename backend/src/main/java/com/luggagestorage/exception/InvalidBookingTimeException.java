package com.luggagestorage.exception;

import java.time.LocalDateTime;

public class InvalidBookingTimeException extends RuntimeException {

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public InvalidBookingTimeException(String message) {
        super(message);
    }

    public InvalidBookingTimeException(String message, LocalDateTime startTime, LocalDateTime endTime) {
        super(message);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public InvalidBookingTimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
