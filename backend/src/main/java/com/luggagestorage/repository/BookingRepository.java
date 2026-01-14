package com.luggagestorage.repository;

import com.luggagestorage.model.Booking;
import com.luggagestorage.model.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerId(Long customerId);

    List<Booking> findByLockerId(Long lockerId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByCustomerIdAndStatus(Long customerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.locker.id = :lockerId " +
            "AND b.status = 'ACTIVE' " +
            "AND ((b.startDatetime < :end AND b.endDatetime > :start))")
    List<Booking> findOverlappingBookings(@Param("lockerId") Long lockerId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    default List<Booking> findActiveBookings() {
        return findByStatus(BookingStatus.ACTIVE);
    }

    @Query("SELECT b FROM Booking b WHERE b.customer.id = :customerId " +
            "AND b.startDatetime >= :startDate AND b.endDatetime <= :endDate")
    List<Booking> findByCustomerIdAndDateRange(@Param("customerId") Long customerId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.status = 'ACTIVE' AND b.endDatetime < :currentTime")
    List<Booking> findExpiredActiveBookings(@Param("currentTime") LocalDateTime currentTime);
}
