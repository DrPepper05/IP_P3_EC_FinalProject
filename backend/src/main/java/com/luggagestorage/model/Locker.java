package com.luggagestorage.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.luggagestorage.model.enums.Size;
import com.luggagestorage.model.enums.Status;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Locker entity representing storage lockers in the system.
 * Part of the minimum 4 classes requirement.
 */
@Entity
@Table(name = "lockers")
public class Locker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Locker number is required")
    @Column(name = "locker_number", unique = true, nullable = false)
    private String lockerNumber;

    @NotNull(message = "Size is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Size size;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @NotNull(message = "Hourly rate is required")
    @Min(value = 0, message = "Hourly rate must be positive")
    @Column(name = "hourly_rate", nullable = false)
    private Double hourlyRate;

    @NotBlank(message = "Location name is required")
    @Column(name = "location_name", nullable = false)
    private String locationName;

    @NotBlank(message = "Address is required")
    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @NotNull(message = "Latitude is required")
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @NotBlank(message = "Floor is required")
    @Column(name = "floor", nullable = false)
    private String floor;

    @NotBlank(message = "Section is required")
    @Column(name = "section", nullable = false)
    private String section;

    /**
     * Version field for optimistic locking.
     * Prevents concurrent modifications and race conditions.
     */
    @Version
    @Column(name = "version")
    private Long version = 0L;

    /**
     * One-to-Many relationship with Booking.
     * Part of the minimum 2 collections requirement.
     */
    @OneToMany(mappedBy = "locker", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();

    public Locker() {
    }

    public Locker(String lockerNumber, Size size, Status status, Double hourlyRate) {
        this.lockerNumber = lockerNumber;
        this.size = size;
        this.status = status;
        this.hourlyRate = hourlyRate;
    }

    public Locker(String lockerNumber, Size size, Status status, Double hourlyRate,
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Required method: Get all bookings for this locker.
     * Returns the collection of bookings (part of minimum 2 collections requirement).
     *
     * @return List of bookings associated with this locker
     */
    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    /**
     * Add a booking to this locker's bookings.
     */
    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setLocker(this);
    }

    /**
     * Remove a booking from this locker's bookings.
     */
    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setLocker(null);
    }

    /**
     * Required method: Check if the locker is available.
     *
     * @return true if the locker status is AVAILABLE, false otherwise
     */
    public boolean isAvailable() {
        return this.status == Status.AVAILABLE;
    }

    /**
     * Required method: Update the locker status.
     *
     * @param newStatus The new status to set
     */
    public void updateStatus(Status newStatus) {
        this.status = newStatus;
    }

    /**
     * Mark the locker as available.
     */
    public void markAsAvailable() {
        this.status = Status.AVAILABLE;
    }

    /**
     * Mark the locker as occupied.
     */
    public void markAsOccupied() {
        this.status = Status.OCCUPIED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Locker locker = (Locker) o;
        return Objects.equals(id, locker.id) && Objects.equals(lockerNumber, locker.lockerNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lockerNumber);
    }

    @Override
    public String toString() {
        return "Locker{" +
                "id=" + id +
                ", lockerNumber='" + lockerNumber + '\'' +
                ", size=" + size +
                ", status=" + status +
                ", hourlyRate=" + hourlyRate +
                ", locationName='" + locationName + '\'' +
                ", address='" + address + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", floor='" + floor + '\'' +
                ", section='" + section + '\'' +
                '}';
    }
}
