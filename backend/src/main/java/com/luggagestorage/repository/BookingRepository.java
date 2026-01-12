package com.luggagestorage.repository;

import com.luggagestorage.model.Booking;
import com.luggagestorage.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for Booking entity.
 * Provides data access methods for booking management.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find all bookings for a specific customer.
     *
     * @param customerId The customer ID
     * @return List of bookings for the customer
     */
    List<Booking> findByCustomerId(Long customerId);

    /**
     * Find all bookings for a specific locker.
     *
     * @param lockerId The locker ID
     * @return List of bookings for the locker
     */
    List<Booking> findByLockerId(Long lockerId);

    /**
     * Find all bookings with a specific status.
     *
     * @param status The booking status
     * @return List of bookings with the specified status
     */
    List<Booking> findByStatus(BookingStatus status);

    /**
     * Find all active bookings for a specific customer.
     *
     * @param customerId The customer ID
     * @param status     The booking status
     * @return List of active bookings for the customer
     */
    List<Booking> findByCustomerIdAndStatus(Long customerId, BookingStatus status);

    /**
     * Find all bookings for a specific locker within a time range.
     * Used to check for overlapping bookings.
     *
     * @param lockerId The locker ID
     * @param start    Start datetime
     * @param end      End datetime
     * @return List of overlapping bookings
     */
    @Query("SELECT b FROM Booking b WHERE b.locker.id = :lockerId " +
            "AND b.status = 'ACTIVE' " +
            "AND ((b.startDatetime < :end AND b.endDatetime > :start))")
    List<Booking> findOverlappingBookings(@Param("lockerId") Long lockerId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    /**
     * Find all active bookings.
     *
     * @return List of active bookings
     */
    default List<Booking> findActiveBookings() {
        return findByStatus(BookingStatus.ACTIVE);
    }

    /**
     * Find bookings by customer and date range.
     *
     * @param customerId The customer ID
     * @param startDate  Start date
     * @param endDate    End date
     * @return List of bookings in the date range
     */
    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId " +
            "AND b.startDatetime >= :startDate AND b.endDatetime <= :endDate")
    List<Booking> findByCustomerIdAndDateRange(@Param("customerId") Long customerId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * Find all ACTIVE bookings that have expired (end datetime has passed).
     * Used by the booking scheduler to automatically complete expired bookings.
     *
     * @param currentTime The current datetime
     * @return List of expired active bookings
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'ACTIVE' AND b.endDatetime < :currentTime")
    List<Booking> findExpiredActiveBookings(@Param("currentTime") LocalDateTime currentTime);
}
