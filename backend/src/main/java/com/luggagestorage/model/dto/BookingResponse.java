package com.luggagestorage.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.luggagestorage.model.enums.BookingStatus;
import com.luggagestorage.model.enums.Size;

import java.time.LocalDateTime;

/**
 * DTO for booking response.
 */
public class BookingResponse {

    private Long id;
    private Long customerId;
    private String customerName;
    private Long lockerId;
    private String lockerNumber;
    private Size lockerSize;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDatetime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDatetime;

    private BookingStatus status;
    private Double totalPrice;
    private Long durationInHours;

    public BookingResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public Size getLockerSize() {
        return lockerSize;
    }

    public void setLockerSize(Size lockerSize) {
        this.lockerSize = lockerSize;
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

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getDurationInHours() {
        return durationInHours;
    }

    public void setDurationInHours(Long durationInHours) {
        this.durationInHours = durationInHours;
    }
}
