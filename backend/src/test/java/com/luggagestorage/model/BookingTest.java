package com.luggagestorage.model;

import com.luggagestorage.model.enums.BookingStatus;
import com.luggagestorage.model.enums.Role;
import com.luggagestorage.model.enums.Size;
import com.luggagestorage.model.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Booking entity.
 * Tests both constructors and business logic functionality as required by the project requirements.
 */
class BookingTest {

    private Person testCustomer;
    private Locker testLocker;

    @BeforeEach
    void setUp() {
        // Create test customer
        testCustomer = new Person("test@example.com", "hashedPass", "Test", "Customer", Role.CUSTOMER);
        testCustomer.setId(1L);

        // Create test locker with hourly rate
        testLocker = new Locker("L001", Size.MEDIUM, Status.AVAILABLE, 5.0);
        testLocker.setId(1L);
    }

    // Test 1: Default constructor
    @Test
    @DisplayName("Test default constructor creates empty Booking")
    void testDefaultConstructor() {
        Booking booking = new Booking();

        assertNotNull(booking, "Booking object should not be null");
        assertNull(booking.getId(), "ID should be null for new booking");
        assertNull(booking.getCustomer(), "Customer should be null");
        assertNull(booking.getLocker(), "Locker should be null");
        assertNull(booking.getStartDatetime(), "Start datetime should be null");
        assertNull(booking.getEndDatetime(), "End datetime should be null");
        assertNull(booking.getStatus(), "Status should be null");
        assertNull(booking.getTotalPrice(), "Total price should be null");
    }

    // Test 2: Parameterized constructor
    @Test
    @DisplayName("Test parameterized constructor initializes all fields correctly")
    void testParameterizedConstructor() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(3);

        Booking booking = new Booking(testCustomer, testLocker, start, end);

        assertNotNull(booking, "Booking object should not be null");
        assertEquals(testCustomer, booking.getCustomer(), "Customer should match");
        assertEquals(testLocker, booking.getLocker(), "Locker should match");
        assertEquals(start, booking.getStartDatetime(), "Start datetime should match");
        assertEquals(end, booking.getEndDatetime(), "End datetime should match");
        assertEquals(BookingStatus.ACTIVE, booking.getStatus(), "Status should be ACTIVE by default");
        assertNotNull(booking.getTotalPrice(), "Total price should be calculated");
        assertEquals(15.0, booking.getTotalPrice(), 0.01, "Price should be 3 hours × $5/hour = $15");
    }

    // Test 3: Functionality - calculatePrice() for multiple hours
    @Test
    @DisplayName("Test calculatePrice() calculates correct price for multiple hours")
    void testCalculatePrice_MultipleHours() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 14, 0); // 4 hours later

        Booking booking = new Booking(testCustomer, testLocker, start, end);

        double price = booking.calculatePrice();

        assertEquals(20.0, price, 0.01, "Price should be 4 hours × $5/hour = $20");
    }

    // Test 4: Functionality - calculatePrice() with minimum 1 hour
    @Test
    @DisplayName("Test calculatePrice() charges minimum 1 hour for short duration")
    void testCalculatePrice_MinimumOneHour() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 10, 30); // Only 30 minutes

        Booking booking = new Booking(testCustomer, testLocker, start, end);

        double price = booking.calculatePrice();

        assertEquals(5.0, price, 0.01, "Price should be minimum 1 hour × $5/hour = $5");
    }

    // Test 5: Functionality - calculatePrice() returns 0 for null values
    @Test
    @DisplayName("Test calculatePrice() returns 0 when fields are null")
    void testCalculatePrice_NullValues() {
        Booking booking = new Booking();

        double price = booking.calculatePrice();

        assertEquals(0.0, price, 0.01, "Price should be 0 when fields are null");
    }

    // Test 6: Functionality - updateTotalPrice()
    @Test
    @DisplayName("Test updateTotalPrice() recalculates and updates price")
    void testUpdateTotalPrice() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0); // 2 hours

        Booking booking = new Booking(testCustomer, testLocker, start, end);
        assertEquals(10.0, booking.getTotalPrice(), 0.01, "Initial price should be $10");

        // Change end time to 5 hours later
        booking.setEndDatetime(LocalDateTime.of(2024, 1, 1, 15, 0));
        booking.updateTotalPrice();

        assertEquals(25.0, booking.getTotalPrice(), 0.01, "Updated price should be 5 hours × $5/hour = $25");
    }

    // Test 7: Functionality - cancel() method
    @Test
    @DisplayName("Test cancel() sets status to CANCELLED")
    void testCancel() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        Booking booking = new Booking(testCustomer, testLocker, start, end);

        assertEquals(BookingStatus.ACTIVE, booking.getStatus(), "Initial status should be ACTIVE");

        booking.cancel();

        assertEquals(BookingStatus.CANCELLED, booking.getStatus(), "Status should be CANCELLED after cancellation");
        assertTrue(booking.isCancelled(), "isCancelled() should return true");
    }

    // Test 8: Functionality - complete() method
    @Test
    @DisplayName("Test complete() sets status to COMPLETED")
    void testComplete() {
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        Booking booking = new Booking(testCustomer, testLocker, start, end);

        assertEquals(BookingStatus.ACTIVE, booking.getStatus(), "Initial status should be ACTIVE");

        booking.complete();

        assertEquals(BookingStatus.COMPLETED, booking.getStatus(), "Status should be COMPLETED");
        assertTrue(booking.isCompleted(), "isCompleted() should return true");
    }

    // Test 9: Functionality - isActive() method
    @Test
    @DisplayName("Test isActive() returns true for ACTIVE status")
    void testIsActive() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        Booking booking = new Booking(testCustomer, testLocker, start, end);

        assertTrue(booking.isActive(), "isActive() should return true for ACTIVE status");

        booking.cancel();
        assertFalse(booking.isActive(), "isActive() should return false after cancellation");
    }

    // Test 10: Functionality - isCancelled() method
    @Test
    @DisplayName("Test isCancelled() returns true for CANCELLED status")
    void testIsCancelled() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);
        Booking booking = new Booking(testCustomer, testLocker, start, end);

        assertFalse(booking.isCancelled(), "isCancelled() should return false initially");

        booking.cancel();
        assertTrue(booking.isCancelled(), "isCancelled() should return true after cancellation");
    }

    // Test 11: Functionality - isCompleted() method
    @Test
    @DisplayName("Test isCompleted() returns true for COMPLETED status")
    void testIsCompleted() {
        LocalDateTime start = LocalDateTime.now().minusHours(3);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        Booking booking = new Booking(testCustomer, testLocker, start, end);

        assertFalse(booking.isCompleted(), "isCompleted() should return false initially");

        booking.complete();
        assertTrue(booking.isCompleted(), "isCompleted() should return true after completion");
    }

    // Test 12: Functionality - getDurationInHours()
    @Test
    @DisplayName("Test getDurationInHours() returns correct duration")
    void testGetDurationInHours() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 16, 0); // 6 hours later

        Booking booking = new Booking(testCustomer, testLocker, start, end);

        long duration = booking.getDurationInHours();

        assertEquals(6L, duration, "Duration should be 6 hours");
    }

    // Test 13: Functionality - getDurationInHours() with null values
    @Test
    @DisplayName("Test getDurationInHours() returns 0 for null datetimes")
    void testGetDurationInHours_NullValues() {
        Booking booking = new Booking();

        long duration = booking.getDurationInHours();

        assertEquals(0L, duration, "Duration should be 0 for null datetimes");
    }

    // Test 14: Functionality - equals() method
    @Test
    @DisplayName("Test equals() method compares bookings correctly")
    void testEquals() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        Booking booking1 = new Booking(testCustomer, testLocker, start, end);
        booking1.setId(1L);

        Booking booking2 = new Booking(testCustomer, testLocker, start, end);
        booking2.setId(1L);

        Booking booking3 = new Booking(testCustomer, testLocker, start, end);
        booking3.setId(2L);

        assertEquals(booking1, booking2, "Bookings with same ID should be equal");
        assertNotEquals(booking1, booking3, "Bookings with different ID should not be equal");
        assertNotEquals(booking1, null, "Booking should not equal null");
        assertEquals(booking1, booking1, "Booking should equal itself");
    }

    // Test 15: Functionality - hashCode() method
    @Test
    @DisplayName("Test hashCode() returns consistent values")
    void testHashCode() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        Booking booking1 = new Booking(testCustomer, testLocker, start, end);
        booking1.setId(1L);

        Booking booking2 = new Booking(testCustomer, testLocker, start, end);
        booking2.setId(1L);

        assertEquals(booking1.hashCode(), booking2.hashCode(),
                "Equal bookings should have the same hash code");
    }

    // Test 16: Functionality - toString() method
    @Test
    @DisplayName("Test toString() returns proper string representation")
    void testToString() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0);
        Booking booking = new Booking(testCustomer, testLocker, start, end);
        booking.setId(1L);

        String result = booking.toString();

        assertNotNull(result, "toString() should not return null");
        assertTrue(result.contains("Booking{"), "toString() should contain class name");
        assertTrue(result.contains("id=1"), "toString() should contain ID");
        assertTrue(result.contains("customerId=1"), "toString() should contain customer ID");
        assertTrue(result.contains("lockerId=1"), "toString() should contain locker ID");
        assertTrue(result.contains("status=ACTIVE"), "toString() should contain status");
    }

    // Test 17: Functionality - setters work correctly
    @Test
    @DisplayName("Test all setters modify fields correctly")
    void testSetters() {
        Booking booking = new Booking();
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 0);

        booking.setId(100L);
        booking.setCustomer(testCustomer);
        booking.setLocker(testLocker);
        booking.setStartDatetime(start);
        booking.setEndDatetime(end);
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setTotalPrice(50.0);
        booking.setVersion(3L);

        assertEquals(100L, booking.getId(), "ID should be updated");
        assertEquals(testCustomer, booking.getCustomer(), "Customer should be updated");
        assertEquals(testLocker, booking.getLocker(), "Locker should be updated");
        assertEquals(start, booking.getStartDatetime(), "Start datetime should be updated");
        assertEquals(end, booking.getEndDatetime(), "End datetime should be updated");
        assertEquals(BookingStatus.COMPLETED, booking.getStatus(), "Status should be updated");
        assertEquals(50.0, booking.getTotalPrice(), 0.01, "Total price should be updated");
        assertEquals(3L, booking.getVersion(), "Version should be updated");
    }
}
