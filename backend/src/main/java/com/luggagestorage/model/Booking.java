package com.luggagestorage.model;

import com.luggagestorage.model.enums.BookingStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Customer is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Person customer;

    @NotNull(message = "Locker is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locker_id", nullable = false)
    private Locker locker;

    @NotNull(message = "Start datetime is required")
    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @NotNull(message = "End datetime is required")
    @Column(name = "end_datetime", nullable = false)
    private LocalDateTime endDatetime;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @NotNull(message = "Total price is required")
    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    public Booking() {
    }

    public Booking(Person customer, Locker locker, LocalDateTime startDatetime, LocalDateTime endDatetime) {
        this.customer = customer;
        this.locker = locker;
        this.startDatetime = startDatetime;
        this.endDatetime = endDatetime;
        this.status = BookingStatus.ACTIVE;
        this.totalPrice = calculatePrice();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Person getCustomer() {
        return customer;
    }

    public void setCustomer(Person customer) {
        this.customer = customer;
    }

    public Locker getLocker() {
        return locker;
    }

    public void setLocker(Locker locker) {
        this.locker = locker;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public double calculatePrice() {
        if (startDatetime == null || endDatetime == null || locker == null) {
            return 0.0;
        }

        long hours = ChronoUnit.HOURS.between(startDatetime, endDatetime);

        if (hours < 1) {
            hours = 1;
        }

        return hours * locker.getHourlyRate();
    }

    public void updateTotalPrice() {
        this.totalPrice = calculatePrice();
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public void complete() {
        this.status = BookingStatus.COMPLETED;
    }

    public boolean isActive() {
        return this.status == BookingStatus.ACTIVE;
    }

    public boolean isCancelled() {
        return this.status == BookingStatus.CANCELLED;
    }

    public boolean isCompleted() {
        return this.status == BookingStatus.COMPLETED;
    }

    public long getDurationInHours() {
        if (startDatetime == null || endDatetime == null) {
            return 0;
        }
        return ChronoUnit.HOURS.between(startDatetime, endDatetime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", customerId=" + (customer != null ? customer.getId() : null) +
                ", lockerId=" + (locker != null ? locker.getId() : null) +
                ", startDatetime=" + startDatetime +
                ", endDatetime=" + endDatetime +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
