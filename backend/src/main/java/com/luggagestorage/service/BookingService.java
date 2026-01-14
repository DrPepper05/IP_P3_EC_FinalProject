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

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Booking createBooking(Long customerId, Long lockerId, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Creating new booking for customer {} and locker {}", customerId, lockerId);

        try {

            validateBookingTimes(startTime, endTime);

            Person customer = personService.getPersonById(customerId);

            Locker locker = lockerRepository.findByIdWithLock(lockerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Locker", "id", lockerId));

            checkForOverlappingBookings(lockerId, startTime, endTime);

            Booking booking = new Booking(customer, locker, startTime, endTime);
            booking.setStatus(BookingStatus.ACTIVE);

            booking.updateTotalPrice();

            Booking savedBooking = bookingRepository.save(booking);

            lockerService.updateLockerStatus(lockerId, Status.OCCUPIED);

            saveToFile();

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

    private void checkForOverlappingBookings(Long lockerId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(lockerId, startTime, endTime);

        if (!overlappingBookings.isEmpty()) {
            throw new LockerNotAvailableException(
                    "Locker is already booked during the requested time period", lockerId);
        }
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public List<Booking> getBookingsByCustomer(Long customerId) {
        return bookingRepository.findByCustomerId(customerId);
    }

    public List<Booking> getActiveBookingsByCustomer(Long customerId) {
        return bookingRepository.findByCustomerIdAndStatus(customerId, BookingStatus.ACTIVE);
    }

    public List<Booking> getBookingsByLocker(Long lockerId) {
        return bookingRepository.findByLockerId(lockerId);
    }

    public List<Booking> getActiveBookings() {
        return bookingRepository.findByStatus(BookingStatus.ACTIVE);
    }

    public List<Booking> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status);
    }

    public Booking updateBooking(Long id, LocalDateTime startTime, LocalDateTime endTime) {
        logger.info("Updating booking with ID: {}", id);

        Booking booking = getBookingById(id);

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalArgumentException("Can only update active bookings");
        }

        if (startTime != null && endTime != null) {
            validateBookingTimes(startTime, endTime);

            List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                    booking.getLocker().getId(), startTime, endTime);

            overlappingBookings.removeIf(b -> b.getId().equals(id));

            if (!overlappingBookings.isEmpty()) {
                throw new LockerNotAvailableException(
                        "Locker is already booked during the requested time period",
                        booking.getLocker().getId());
            }

            booking.setStartDatetime(startTime);
            booking.setEndDatetime(endTime);

            booking.updateTotalPrice();
        }

        Booking updatedBooking = bookingRepository.save(booking);
        saveToFile();

        broadcastBookingEvent(updatedBooking, "UPDATED", "Booking updated");

        logger.info("Booking updated successfully with ID: {}", updatedBooking.getId());
        return updatedBooking;
    }

    public Booking cancelBooking(Long id) {
        logger.info("Cancelling booking with ID: {}", id);

        Booking booking = getBookingById(id);

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalArgumentException("Can only cancel active bookings");
        }

        booking.cancel();

        Locker locker = booking.getLocker();
        lockerService.updateLockerStatus(locker.getId(), Status.AVAILABLE);

        Booking cancelledBooking = bookingRepository.save(booking);
        saveToFile();

        broadcastBookingEvent(cancelledBooking, "CANCELLED", "Booking cancelled");
        broadcastLockerAvailabilityEvent(locker, "Locker now available");

        logger.info("Booking cancelled successfully with ID: {}", id);
        return cancelledBooking;
    }

    public Booking completeBooking(Long id) {
        logger.info("Completing booking with ID: {}", id);

        Booking booking = getBookingById(id);

        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw new IllegalArgumentException("Can only complete active bookings");
        }

        booking.complete();

        Locker locker = booking.getLocker();
        lockerService.updateLockerStatus(locker.getId(), Status.AVAILABLE);

        Booking completedBooking = bookingRepository.save(booking);
        saveToFile();

        broadcastBookingEvent(completedBooking, "COMPLETED", "Booking completed");
        broadcastLockerAvailabilityEvent(locker, "Locker now available");

        logger.info("Booking completed successfully with ID: {}", id);
        return completedBooking;
    }

    public void deleteBooking(Long id) {
        logger.info("Deleting booking with ID: {}", id);

        Booking booking = getBookingById(id);

        if (booking.getStatus() == BookingStatus.ACTIVE) {
            lockerService.updateLockerStatus(booking.getLocker().getId(), Status.AVAILABLE);
        }

        bookingRepository.delete(booking);
        saveToFile();
        logger.info("Booking deleted successfully with ID: {}", id);
    }

    private void saveToFile() {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            fileStorageService.saveToFile(bookings, BOOKINGS_FILE);
        } catch (IOException e) {
            logger.error("Failed to save bookings to file: {}", e.getMessage());

        }
    }

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

        }
    }

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

        }
    }
}
