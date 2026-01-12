package com.luggagestorage.repository;

import com.luggagestorage.model.Locker;
import com.luggagestorage.model.enums.Size;
import com.luggagestorage.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Locker entity.
 * Provides data access methods for locker management.
 */
@Repository
public interface LockerRepository extends JpaRepository<Locker, Long> {

    /**
     * Find a locker by locker number.
     *
     * @param lockerNumber The locker number
     * @return Optional containing the locker if found
     */
    Optional<Locker> findByLockerNumber(String lockerNumber);

    /**
     * Find all lockers with a specific status.
     *
     * @param status The status to filter by
     * @return List of lockers with the specified status
     */
    List<Locker> findByStatus(Status status);

    /**
     * Find all available lockers.
     *
     * @return List of available lockers
     */
    default List<Locker> findAvailableLockers() {
        return findByStatus(Status.AVAILABLE);
    }

    /**
     * Find all lockers of a specific size.
     *
     * @param size The size to filter by
     * @return List of lockers with the specified size
     */
    List<Locker> findBySize(Size size);

    /**
     * Find all lockers by size and status.
     *
     * @param size   The size to filter by
     * @param status The status to filter by
     * @return List of lockers matching the criteria
     */
    List<Locker> findBySizeAndStatus(Size size, Status status);

    /**
     * Check if a locker exists with the given locker number.
     *
     * @param lockerNumber The locker number
     * @return true if exists, false otherwise
     */
    boolean existsByLockerNumber(String lockerNumber);

    /**
     * Find lockers that are truly available (no active bookings).
     * This checks both the status field AND ensures no active bookings exist.
     *
     * @return List of truly available lockers
     */
    @Query("SELECT l FROM Locker l WHERE l.status = 'AVAILABLE' " +
           "AND NOT EXISTS (SELECT b FROM Booking b WHERE b.locker = l " +
           "AND b.status = 'ACTIVE' AND b.endDatetime > CURRENT_TIMESTAMP)")
    List<Locker> findTrulyAvailableLockers();

    /**
     * Find lockers available for a specific time range.
     * Checks that the locker has no overlapping active bookings in the specified period.
     *
     * @param startTime Start of the desired booking period
     * @param endTime End of the desired booking period
     * @return List of lockers available for the time range
     */
    @Query("SELECT l FROM Locker l WHERE l.status = 'AVAILABLE' " +
           "AND NOT EXISTS (SELECT b FROM Booking b WHERE b.locker = l " +
           "AND b.status = 'ACTIVE' " +
           "AND ((b.startDatetime < :endTime AND b.endDatetime > :startTime)))")
    List<Locker> findAvailableForTimeRange(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * Find a locker by ID with pessimistic write lock to prevent concurrent modifications.
     * Used during booking creation to prevent race conditions.
     *
     * @param id The locker ID
     * @return Optional containing the locked locker if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Locker l WHERE l.id = :id")
    Optional<Locker> findByIdWithLock(@Param("id") Long id);
}
