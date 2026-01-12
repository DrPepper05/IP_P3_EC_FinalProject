package com.luggagestorage.service;

import com.luggagestorage.exception.InvalidBookingTimeException;
import com.luggagestorage.exception.LockerNotAvailableException;
import com.luggagestorage.exception.ResourceNotFoundException;
import com.luggagestorage.model.Booking;
import com.luggagestorage.model.Locker;
import com.luggagestorage.model.Person;
import com.luggagestorage.model.enums.BookingStatus;
import com.luggagestorage.model.enums.Role;
import com.luggagestorage.model.enums.Size;
import com.luggagestorage.model.enums.Status;
import com.luggagestorage.repository.BookingRepository;
import com.luggagestorage.repository.LockerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Functional unit tests for BookingService.
 * Tests business logic, validation, and integration between components.
 */
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private LockerRepository lockerRepository;

    @Mock
    private PersonService personService;

    @Mock
    private LockerService lockerService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BookingService bookingService;

    private Person testCustomer;
    private Locker testLocker;
    private LocalDateTime futureStart;
    private LocalDateTime futureEnd;

    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = new Person("test@example.com", "hashedPass", "Test", "Customer", Role.CUSTOMER);
        testCustomer.setId(1L);

        // Create test locker
        testLocker = new Locker("L001", Size.MEDIUM, Status.AVAILABLE, 5.0);
        testLocker.setId(1L);

        // Set up test times
        futureStart = LocalDateTime.now().plusHours(2);
        futureEnd = futureStart.plusHours(3);
    }

    // Test 1: Functional test - Get all bookings
    @Test
    @DisplayName("Test getAllBookings returns all bookings")
    void testGetAllBookings() {
        // Create test data
        Booking booking1 = new Booking(testCustomer, testLocker, futureStart, futureEnd);
        Booking booking2 = new Booking(testCustomer, testLocker, futureStart.plusDays(1), futureEnd.plusDays(1));
        List<Booking> mockBookings = Arrays.asList(booking1, booking2);

        // Mock repository
        when(bookingRepository.findAll()).thenReturn(mockBookings);

        // Execute
        List<Booking> result = bookingService.getAllBookings();

        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 bookings");
        verify(bookingRepository, times(1)).findAll();
    }

    // Test 2: Functional test - Get booking by ID (success)
    @Test
    @DisplayName("Test getBookingById returns booking when found")
    void testGetBookingById_Success() {
        // Create test booking
        Booking testBooking = new Booking(testCustomer, testLocker, futureStart, futureEnd);
        testBooking.setId(1L);

        // Mock repository
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        // Execute
        Booking result = bookingService.getBookingById(1L);

        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(1L, result.getId(), "Booking ID should match");
        assertEquals(testCustomer, result.getCustomer(), "Customer should match");
        assertEquals(testLocker, result.getLocker(), "Locker should match");
        verify(bookingRepository, times(1)).findById(1L);
    }

    // Test 3: Functional test - Get booking by ID (not found)
    @Test
    @DisplayName("Test getBookingById throws exception when booking not found")
    void testGetBookingById_NotFound() {
        // Mock repository to return empty
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute and verify exception
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.getBookingById(999L);
        }, "Should throw ResourceNotFoundException when booking not found");

        verify(bookingRepository, times(1)).findById(999L);
    }

    // Test 4: Functional test - Get bookings by customer ID
    @Test
    @DisplayName("Test getBookingsByCustomer returns customer's bookings")
    void testGetBookingsByCustomer() {
        // Create test bookings
        Booking booking1 = new Booking(testCustomer, testLocker, futureStart, futureEnd);
        Booking booking2 = new Booking(testCustomer, testLocker, futureStart.plusDays(1), futureEnd.plusDays(1));
        List<Booking> mockBookings = Arrays.asList(booking1, booking2);

        // Mock repository
        when(bookingRepository.findByCustomerId(1L)).thenReturn(mockBookings);

        // Execute
        List<Booking> result = bookingService.getBookingsByCustomer(1L);

        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 bookings");
        verify(bookingRepository, times(1)).findByCustomerId(1L);
    }

    // Test 5: Functional test - Get bookings by locker ID
    @Test
    @DisplayName("Test getBookingsByLocker returns locker's bookings")
    void testGetBookingsByLocker() {
        // Create test bookings
        Booking booking1 = new Booking(testCustomer, testLocker, futureStart, futureEnd);
        Booking booking2 = new Booking(testCustomer, testLocker, futureStart.plusDays(1), futureEnd.plusDays(1));
        List<Booking> mockBookings = Arrays.asList(booking1, booking2);

        // Mock repository
        when(bookingRepository.findByLockerId(1L)).thenReturn(mockBookings);

        // Execute
        List<Booking> result = bookingService.getBookingsByLocker(1L);

        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 bookings");
        verify(bookingRepository, times(1)).findByLockerId(1L);
    }

    // Test 6: Functional test - Get active bookings
    @Test
    @DisplayName("Test getActiveBookings returns only active bookings")
    void testGetActiveBookings() {
        // Create test bookings
        Booking activeBooking1 = new Booking(testCustomer, testLocker, futureStart, futureEnd);
        activeBooking1.setStatus(BookingStatus.ACTIVE);
        Booking activeBooking2 = new Booking(testCustomer, testLocker, futureStart.plusDays(1), futureEnd.plusDays(1));
        activeBooking2.setStatus(BookingStatus.ACTIVE);
        List<Booking> mockBookings = Arrays.asList(activeBooking1, activeBooking2);

        // Mock repository
        when(bookingRepository.findByStatus(BookingStatus.ACTIVE)).thenReturn(mockBookings);

        // Execute
        List<Booking> result = bookingService.getActiveBookings();

        // Verify
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 active bookings");
        assertTrue(result.stream().allMatch(Booking::isActive), "All bookings should be active");
        verify(bookingRepository, times(1)).findByStatus(BookingStatus.ACTIVE);
    }

    // Test 7: Functional test - Delete booking (success)
    @Test
    @DisplayName("Test deleteBooking removes booking when found")
    void testDeleteBooking_Success() {
        // Create test booking
        Booking testBooking = new Booking(testCustomer, testLocker, futureStart, futureEnd);
        testBooking.setId(1L);

        // Mock repository
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        doNothing().when(bookingRepository).delete(any(Booking.class));

        // Execute
        assertDoesNotThrow(() -> bookingService.deleteBooking(1L),
                "Should not throw exception when deleting existing booking");

        // Verify
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).delete(testBooking);
    }

    // Test 8: Functional test - Delete booking (not found)
    @Test
    @DisplayName("Test deleteBooking throws exception when booking not found")
    void testDeleteBooking_NotFound() {
        // Mock repository to return empty
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        // Execute and verify exception
        assertThrows(ResourceNotFoundException.class, () -> {
            bookingService.deleteBooking(999L);
        }, "Should throw ResourceNotFoundException when booking not found");

        verify(bookingRepository, times(1)).findById(999L);
        verify(bookingRepository, never()).delete(any(Booking.class));
    }

    // Test 9: Business logic test - Booking price calculation
    @Test
    @DisplayName("Test booking price is calculated correctly on creation")
    void testBookingPriceCalculation() {
        // Create booking with 3-hour duration
        Booking booking = new Booking(testCustomer, testLocker, futureStart, futureEnd);

        // Verify price calculation
        double expectedPrice = 3.0 * 5.0; // 3 hours × $5/hour
        assertEquals(expectedPrice, booking.getTotalPrice(), 0.01,
                "Price should be calculated as duration × hourly rate");
    }

    // Test 10: Business logic test - Booking status transitions
    @Test
    @DisplayName("Test booking status can transition from ACTIVE to CANCELLED")
    void testBookingStatusTransition_ActiveToCancelled() {
        Booking booking = new Booking(testCustomer, testLocker, futureStart, futureEnd);

        // Initial state
        assertEquals(BookingStatus.ACTIVE, booking.getStatus(), "Initial status should be ACTIVE");
        assertTrue(booking.isActive(), "Booking should be active");

        // Cancel booking
        booking.cancel();

        // Verify state after cancellation
        assertEquals(BookingStatus.CANCELLED, booking.getStatus(), "Status should be CANCELLED");
        assertFalse(booking.isActive(), "Booking should not be active");
        assertTrue(booking.isCancelled(), "Booking should be cancelled");
    }

    // Test 11: Business logic test - Booking completion
    @Test
    @DisplayName("Test booking status can transition from ACTIVE to COMPLETED")
    void testBookingStatusTransition_ActiveToCompleted() {
        Booking booking = new Booking(testCustomer, testLocker, futureStart, futureEnd);

        // Initial state
        assertEquals(BookingStatus.ACTIVE, booking.getStatus(), "Initial status should be ACTIVE");

        // Complete booking
        booking.complete();

        // Verify state after completion
        assertEquals(BookingStatus.COMPLETED, booking.getStatus(), "Status should be COMPLETED");
        assertFalse(booking.isActive(), "Booking should not be active");
        assertTrue(booking.isCompleted(), "Booking should be completed");
    }

    // Test 12: Integration test - Booking with customer and locker relationships
    @Test
    @DisplayName("Test booking maintains relationships with customer and locker")
    void testBookingRelationships() {
        Booking booking = new Booking(testCustomer, testLocker, futureStart, futureEnd);

        // Verify relationships
        assertNotNull(booking.getCustomer(), "Customer should not be null");
        assertNotNull(booking.getLocker(), "Locker should not be null");
        assertEquals(testCustomer, booking.getCustomer(), "Customer should match");
        assertEquals(testLocker, booking.getLocker(), "Locker should match");
        assertEquals(testCustomer.getId(), booking.getCustomer().getId(), "Customer ID should match");
        assertEquals(testLocker.getId(), booking.getLocker().getId(), "Locker ID should match");
    }
}
