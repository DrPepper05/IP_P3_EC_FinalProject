package com.luggagestorage.model.dto;

import com.luggagestorage.model.enums.Size;
import com.luggagestorage.model.enums.Status;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * DTO for locker creation and update requests.
 * Includes input validation annotations (optional feature for 1 point).
 */
public class LockerRequest {

    @NotBlank(message = "Locker number is required")
    private String lockerNumber;

    @NotNull(message = "Size is required")
    private Size size;

    @NotNull(message = "Status is required")
    private Status status;

    @NotNull(message = "Hourly rate is required")
    @Min(value = 0, message = "Hourly rate must be positive")
    private Double hourlyRate;

    @NotBlank(message = "Location name is required")
    private String locationName;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotBlank(message = "Floor is required")
    private String floor;

    @NotBlank(message = "Section is required")
    private String section;

    // Constructors
    public LockerRequest() {
    }

    public LockerRequest(String lockerNumber, Size size, Status status, Double hourlyRate) {
        this.lockerNumber = lockerNumber;
        this.size = size;
        this.status = status;
        this.hourlyRate = hourlyRate;
    }

    public LockerRequest(String lockerNumber, Size size, Status status, Double hourlyRate,
                         String locationName, String address, Double latitude, Double longitude,
                         String floor, String section) {
        this.lockerNumber = lockerNumber;
        this.size = size;
        this.status = status;
        this.hourlyRate = hourlyRate;
        this.locationName = locationName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.floor = floor;
        this.section = section;
    }

    // Getters and Setters
    public String getLockerNumber() {
        return lockerNumber;
    }

    public void setLockerNumber(String lockerNumber) {
        this.lockerNumber = lockerNumber;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }
}
