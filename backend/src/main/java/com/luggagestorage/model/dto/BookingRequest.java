package com.luggagestorage.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for booking creation and update requests.
 * Includes input validation annotations (optional feature for 1 point).
 */
public class BookingRequest {

    @NotNull(message = "Locker ID is required")
    private Long lockerId;

    @NotNull(message = "Start datetime is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime startDatetime;

    @NotNull(message = "End datetime is required")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime endDatetime;

    // Constructors
    public BookingRequest() {
    }

    public BookingRequest(Long lockerId, LocalDateTime startDatetime, LocalDateTime endDatetime) {
        this.lockerId = lockerId;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
    }

    // Getters and Setters
    public Long getLockerId() {
        return lockerId;
    }

    public void setLockerId(Long lockerId) {
        this.lockerId = lockerId;
    }

    public LocalDateTime getStartDatetime() {
        return startDatetime;
    }

    public void setStartDatetime(LocalDateTime startDatetime) {
        this.startDatetime = startDatetime;
    }

    public LocalDateTime getEndDatetime() {
        return endDatetime;
    }

    public void setEndDatetime(LocalDateTime endDatetime) {
        this.endDatetime = endDatetime;
    }
}
