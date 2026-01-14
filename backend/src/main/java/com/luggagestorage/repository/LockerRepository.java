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

@Repository
public interface LockerRepository extends JpaRepository<Locker, Long> {

    Optional<Locker> findByLockerNumber(String lockerNumber);

    List<Locker> findByStatus(Status status);

    default List<Locker> findAvailableLockers() {
        return findByStatus(Status.AVAILABLE);
    }

    List<Locker> findBySize(Size size);

    List<Locker> findBySizeAndStatus(Size size, Status status);

    boolean existsByLockerNumber(String lockerNumber);

    @Query("SELECT l FROM Locker l WHERE l.status = 'AVAILABLE' " +
           "AND NOT EXISTS (SELECT b FROM Booking b WHERE b.locker = l " +
           "AND b.status = 'ACTIVE' AND b.endDatetime > CURRENT_TIMESTAMP)")
    List<Locker> findTrulyAvailableLockers();

    @Query("SELECT l FROM Locker l WHERE l.status = 'AVAILABLE' " +
           "AND NOT EXISTS (SELECT b FROM Booking b WHERE b.locker = l " +
           "AND b.status = 'ACTIVE' " +
           "AND ((b.startDatetime < :endTime AND b.endDatetime > :startTime)))")
    List<Locker> findAvailableForTimeRange(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Locker l WHERE l.id = :id")
    Optional<Locker> findByIdWithLock(@Param("id") Long id);
}
