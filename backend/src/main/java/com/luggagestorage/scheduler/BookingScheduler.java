package com.luggagestorage.scheduler;

import com.luggagestorage.model.Booking;
import com.luggagestorage.repository.BookingRepository;
import com.luggagestorage.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task to automatically complete expired bookings.
 * Runs periodically to check for ACTIVE bookings that have passed their end datetime
 * and automatically marks them as COMPLETED, freeing up lockers for new bookings.
 */
@Component
public class BookingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BookingScheduler.class);

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Value("${scheduler.booking.enabled:true}")
    private boolean schedulerEnabled;

    /**
     * Automatically completes expired ACTIVE bookings.
     * Runs according to the cron expression configured in application.properties.
     * Default: Every 5 minutes (0 *\/5 * * * *)
     *
     * Process:
     * 1. Query all ACTIVE bookings where endDatetime < current time
     * 2. Call bookingService.completeBooking() for each expired booking
     * 3. This automatically:
     *    - Updates booking status to COMPLETED
     *    - Updates locker status to AVAILABLE
     *    - Broadcasts WebSocket events for real-time UI updates
     *    - Saves changes to file storage
     */
    @Scheduled(cron = "${scheduler.booking.cron:0 */5 * * * *}")
    @Transactional
    public void autoCompleteExpiredBookings() {
        if (!schedulerEnabled) {
            logger.debug("Booking scheduler is disabled, skipping auto-complete task");
            return;
        }

        logger.info("Running scheduled task: Auto-complete expired bookings");

        try {
            // Get current time
            LocalDateTime currentTime = LocalDateTime.now();
            logger.debug("Current time: {}", currentTime);

            // Find all expired active bookings
            List<Booking> expiredBookings = bookingRepository.findExpiredActiveBookings(currentTime);

            if (expiredBookings.isEmpty()) {
                logger.info("No expired bookings found");
                return;
            }

            logger.info("Found {} expired booking(s) to auto-complete", expiredBookings.size());

            // Complete each expired booking
            int successCount = 0;
            int failureCount = 0;

            for (Booking booking : expiredBookings) {
                try {
                    logger.info("Auto-completing booking ID: {} (Locker: {}, End time: {})",
                            booking.getId(),
                            booking.getLocker().getLockerNumber(),
                            booking.getEndDatetime());

                    bookingService.completeBooking(booking.getId());
                    successCount++;

                    logger.info("Successfully completed booking ID: {}", booking.getId());

                } catch (Exception e) {
                    failureCount++;
                    logger.error("Failed to auto-complete booking ID: {}. Error: {}",
                            booking.getId(), e.getMessage(), e);
                }
            }

            // Log summary
            logger.info("Auto-complete task completed. Success: {}, Failures: {}, Total: {}",
                    successCount, failureCount, expiredBookings.size());

        } catch (Exception e) {
            logger.error("Error occurred during auto-complete task: {}", e.getMessage(), e);
        }
    }
}
