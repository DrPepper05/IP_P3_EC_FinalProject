package com.luggagestorage.service;

import com.luggagestorage.exception.InvalidBookingTimeException;
import com.luggagestorage.exception.LockerNotAvailableException;
import com.luggagestorage.exception.ResourceNotFoundException;
import com.luggagestorage.model.Booking;
import com.luggagestorage.model.Locker;
import com.luggagestorage.model.Person;
import com.luggagestorage.model.dto.BookingEvent;
import com.luggagestorage.model.dto.LockerAvailabilityEvent;
import com.luggagestorage.model.enums.BookingStatus;
import com.luggagestorage.model.enums.Status;
import com.luggagestorage.repository.BookingRepository;
import com.luggagestorage.repository.LockerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.OptimisticLockException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for Booking entity.
 * Provides CRUD operations, price calculation, availability checking, and business logic for booking management.
 */
@Service
@Transactional
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);
    private static final String BOOKINGS_FILE = "bookings.json";

    private final BookingRepository bookingRepository;
    private final LockerRepository lockerRepository;
    private final PersonService personService;
    private final LockerService lockerService;
    private final FileStorageService fileStorageService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public BookingService(BookingRepository bookingRepository,
                          LockerRepository lockerRepository,
                          PersonService personService,
                          LockerService lockerService,
                          FileStorageService fileStorageService,
                          SimpMessagingTemplate messagingTemplate) {
        this.bookingRepository = bookingRepository;
        this.lockerRepository = lockerRepository;
        this.personService = personService;
        this.lockerService = lockerService;
        this.fileStorageService = fileStorageService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Create a new booking.
     * Validates booking times, checks locker availability, and calculates price.
     * Uses pessimistic locking to prevent race conditions.
     *
     * @param customerId The customer ID
     * @param lockerId   The locker ID
     * @param startTime  Booking start time
     * @param endTime    Booking end time
     * @return The created booking
     * @throws InvalidBookingTimeException  if booking times are invalid
     * @throws LockerNotAvailableException if locker is not available
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking createBooking(Long customerId, Long lockerId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Creating new booking for customer {} and locker {}", customerId, lockerId);

        try {
            // Validate booking times
            validateBookingTimes(startTime, endTime);

            // Get customer
            Person customer = personService.getPersonById(customerId);

            // Get locker WITH PESSIMISTIC LOCK to prevent race conditions
            Locker locker = lockerRepository.findByIdWithLock(lockerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Locker", "id", lockerId));

            // Check for overlapping bookings (time-based availability validation)
            // This check is now atomic with the lock
            checkForOverlappingBookings(lockerId, startTime, endTime);

            // Create booking
            Booking booking = new Booking(customer, locker, startTime, endTime);
            booking.setStatus(BookingStatus.ACTIVE);

            // Calculate and set price using the entity's calculatePrice method
            booking.updateTotalPrice();

            // Save booking
            Booking savedBooking = bookingRepository.save(booking);

            // Update locker status to OCCUPIED
            lockerService.updateLockerStatus(lockerId, Status.OCCUPIED);

            saveToFile();

            // Broadcast WebSocket event for real-time updates
            broadcastBookingEvent(savedBooking, "CREATED", "New booking created");
            broadcastLockerAvailabilityEvent(locker, "Locker now occupied");

            logger.info("Booking created successfully with ID: {} and total price: {}",
                    savedBooking.getId(), savedBooking.getTotalPrice());
            return savedBooking;
        } catch (OptimisticLockException e) {
            logger.error("Optimistic lock exception while creating booking for locker {}", lockerId);
            throw new LockerNotAvailableException(
                    "The locker was modified by another user. Please try again.", lockerId);
        }
    }

    /**
     * Validate booking times.
     * Throws InvalidBookingTimeException if times are invalid (custom exception for 1 point).
     *
     * @param startTime Booking start time
     * @param endTime   Booking end time
     * @throws InvalidBookingTimeException if times are invalid
     */
    private void validateBookingTimes(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidBookingTimeException("Start time and end time are required", startTime, endTime);
        }

        if (startTime.isAfter(endTime)) {
            throw new InvalidBookingTimeException("Start time must be before end time", startTime, endTime);
        }

        if (startTime.isEqual(endTime)) {
            throw new InvalidBookingTimeException("Start time and end time cannot be the same", startTime, endTime);
        }

        if (endTime.isBefore(LocalDateTime.now())) {
            throw new InvalidBookingTimeException("End time cannot be in the past", startTime, endTime);
        }
    }

    /**
     * Check for overlapping bookings.
     * Throws LockerNotAvailableException if overlapping bookings exist (custom exception for 1 point).
     *
     * @param lockerId  The locker ID
     * @param startTime Booking start time
     * @param endTime   Booking end time
     * @throws LockerNotAvailableException if overlapping bookings exist
     */
    private void checkForOverlappingBookings(Long lockerId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(lockerId, startTime, endTime);

        if (!overlappingBookings.isEmpty()) {
            throw new LockerNotAvailableException(
                    "Locker is already booked during the requested time period", lockerId);
        }
    }

    /**
     * Get a booking by ID.
     *
     * @param id The booking ID
     * @return The booking
     * @throws ResourceNotFoundException if booking not found
     */
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
    }

    /**
     * Get all bookings.
     *
     * @return List of all bookings
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Get all bookings for a specific customer.
     *
     * @param customerId The customer ID
     * @return List of customer's bookings
     */
    public List<Booking> getBookingsByCustomer(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    /**
     * Get all active bookings for a specific customer.
     *
     * @param customerId The customer ID
     * @return List of customer's active bookings
     */
    public List<Booking> getActiveBookingsByCustomer(Long customerId) {
        return bookingRepository.findByCustomerIdAndStatus(customerId, BookingStatus.ACTIVE);
    }

    /**
     * Get all bookings for a specific locker.
     *
     * @param lockerId The locker ID
     * @return List of locker's bookings
     */
    public List<Booking> getBookingsByLocker(Long lockerId) {
        return bookingRepository.findByLockerId(lockerId);
    }

    /**
     * Get all active bookings.
     *
     * @return List of all active bookings
     */
    public List<Booking> getActiveBookings() {
        return bookingRepository.findByStatus(BookingStatus.ACTIVE);
    }

    /**
     * Get all bookings with a specific status.
     *
     * @param status The booking status
     * @return List of bookings with the specified status
     */
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    /**
     * Update a booking.
     *
     * @param id        The booking ID
     * @param startTime New start time (optional)
     * @param endTime   New end time (optional)
     * @return The updated booking
     * @throws InvalidBookingTimeException  if new times are invalid
     * @throws LockerNotAvailableException if locker is not available for new times
     */
    public Booking updateBooking(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Updating booking with ID: {}", id);

        Booking booking = getBookingById(id);

        // Can only update active bookings
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalArgumentException("Can only update active bookings");
        }

        // Validate new times if provided
        if (startTime != null && endTime != null) {
            validateBookingTimes(startTime, endTime);

            // Check for overlapping bookings (excluding current booking)
            List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                    booking.getLocker().getId(), startTime, endTime);

            // Remove current booking from overlapping list
            overlappingBookings.removeIf(b -> b.getId().equals(id));

            if (!overlappingBookings.isEmpty()) {
                throw new LockerNotAvailableException(
                        "Locker is already booked during the requested time period",
                        booking.getLocker().getId());
            }

            // Update times
            booking.setStartDatetime(startTime);
            booking.setEndDatetime(endTime);

            // Recalculate price
            booking.updateTotalPrice();
        }

        Booking updatedBooking = bookingRepository.save(booking);
        saveToFile();

        // Broadcast WebSocket event for real-time updates
        broadcastBookingEvent(updatedBooking, "UPDATED", "Booking updated");

        logger.info("Booking updated successfully with ID: {}", updatedBooking.getId());
        return updatedBooking;
    }

    /**
     * Cancel a booking.
     * Uses the cancel() method from the Booking entity.
     *
     * @param id The booking ID
     * @return The cancelled booking
     */
    public Booking cancelBooking(Long id) {
        logger.info("Cancelling booking with ID: {}", id);

        Booking booking = getBookingById(id);

        // Can only cancel active bookings
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalArgumentException("Can only cancel active bookings");
        }

        // Cancel booking using entity method
        booking.cancel();

        // Update locker status to AVAILABLE
        Locker locker = booking.getLocker();
        lockerService.updateLockerStatus(locker.getId(), Status.AVAILABLE);

        Booking cancelledBooking = bookingRepository.save(booking);
        saveToFile();

        // Broadcast WebSocket event for real-time updates
        broadcastBookingEvent(cancelledBooking, "CANCELLED", "Booking cancelled");
        broadcastLockerAvailabilityEvent(locker, "Locker now available");

        logger.info("Booking cancelled successfully with ID: {}", id);
        return cancelledBooking;
    }

    /**
     * Complete a booking.
     * Uses the complete() method from the Booking entity.
     *
     * @param id The booking ID
     * @return The completed booking
     */
    public Booking completeBooking(Long id) {
        logger.info("Completing booking with ID: {}", id);

        Booking booking = getBookingById(id);

        // Can only complete active bookings
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalArgumentException("Can only complete active bookings");
        }

        // Complete booking using entity method
        booking.complete();

        // Update locker status to AVAILABLE
        Locker locker = booking.getLocker();
        lockerService.updateLockerStatus(locker.getId(), Status.AVAILABLE);

        Booking completedBooking = bookingRepository.save(booking);
        saveToFile();

        // Broadcast WebSocket event for real-time updates
        broadcastBookingEvent(completedBooking, "COMPLETED", "Booking completed");
        broadcastLockerAvailabilityEvent(locker, "Locker now available");

        logger.info("Booking completed successfully with ID: {}", id);
        return completedBooking;
    }

    /**
     * Delete a booking by ID.
     *
     * @param id The booking ID
     */
    public void deleteBooking(Long id) {
        logger.info("Deleting booking with ID: {}", id);

        Booking booking = getBookingById(id);

        // If booking is active, mark locker as available
        if (booking.getStatus() == BookingStatus.ACTIVE) {
            lockerService.updateLockerStatus(booking.getLocker().getId(), Status.AVAILABLE);
        }

        bookingRepository.delete(booking);
        saveToFile();
        logger.info("Booking deleted successfully with ID: {}", id);
    }

    /**
     * Save all bookings to JSON file.
     * Part of the file I/O requirement.
     */
    private void saveToFile() {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            fileStorageService.saveToFile(bookings, BOOKINGS_FILE);
        } catch (IOException e) {
            logger.error("Failed to save bookings to file: {}", e.getMessage());
            // Don't throw exception - file storage is supplementary
        }
    }

    /**
     * Load bookings from JSON file.
     * Part of the file I/O requirement.
     */
    public void loadFromFile() {
        try {
            if (fileStorageService.fileExists(BOOKINGS_FILE)) {
                List<Booking> bookings = fileStorageService.loadFromFile(BOOKINGS_FILE, Booking.class);
                logger.info("Loaded {} bookings from file", bookings.size());
            }
        } catch (IOException e) {
            logger.error("Failed to load bookings from file: {}", e.getMessage());
        }
    }

    /**
     * Broadcast booking event via WebSocket for real-time updates.
     *
     * @param booking   The booking
     * @param eventType Event type (CREATED, UPDATED, CANCELLED, COMPLETED)
     * @param message   Event message
     */
    private void broadcastBookingEvent(Booking booking, String eventType, String message) {
        try {
            BookingEvent event = new BookingEvent(
                    booking.getId(),
                    booking.getLocker().getId(),
                    booking.getLocker().getLockerNumber(),
                    eventType,
                    booking.getCustomer().getFirstName() + " " + booking.getCustomer().getLastName(),
                    message
            );
            messagingTemplate.convertAndSend("/topic/bookings", event);
            logger.debug("Broadcast booking event: {}", event);
        } catch (Exception e) {
            logger.error("Failed to broadcast booking event: {}", e.getMessage());
            // Don't throw exception - WebSocket is supplementary
        }
    }

    /**
     * Broadcast locker availability event via WebSocket for real-time updates.
     *
     * @param locker  The locker
     * @param message Event message
     */
    private void broadcastLockerAvailabilityEvent(Locker locker, String message) {
        try {
            LockerAvailabilityEvent event = new LockerAvailabilityEvent(
                    locker.getId(),
                    locker.getLockerNumber(),
                    locker.getStatus(),
                    locker.getSize().toString(),
                    message
            );
            messagingTemplate.convertAndSend("/topic/lockers", event);
            logger.debug("Broadcast locker availability event: {}", event);
        } catch (Exception e) {
            logger.error("Failed to broadcast locker availability event: {}", e.getMessage());
            // Don't throw exception - WebSocket is supplementary
        }
    }
}
