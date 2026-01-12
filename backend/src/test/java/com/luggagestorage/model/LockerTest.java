package com.luggagestorage.model;

import com.luggagestorage.model.enums.Size;
import com.luggagestorage.model.enums.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Locker entity.
 * Tests all 3 constructors and functionality as required by the project requirements.
 */
class LockerTest {

    // Test 1: Default constructor
    @Test
    @DisplayName("Test default constructor creates empty Locker")
    void testDefaultConstructor() {
        Locker locker = new Locker();

        assertNotNull(locker, "Locker object should not be null");
        assertNull(locker.getId(), "ID should be null for new locker");
        assertNull(locker.getLockerNumber(), "Locker number should be null");
        assertNull(locker.getSize(), "Size should be null");
        assertNull(locker.getStatus(), "Status should be null");
        assertNull(locker.getHourlyRate(), "Hourly rate should be null");
        assertNotNull(locker.getBookings(), "Bookings list should be initialized");
        assertTrue(locker.getBookings().isEmpty(), "Bookings list should be empty");
    }

    // Test 2: Four-parameter constructor
    @Test
    @DisplayName("Test 4-parameter constructor initializes basic fields correctly")
    void testFourParameterConstructor() {
        String lockerNumber = "L001";
        Size size = Size.MEDIUM;
        Status status = Status.AVAILABLE;
        Double hourlyRate = 5.0;

        Locker locker = new Locker(lockerNumber, size, status, hourlyRate);

        assertNotNull(locker, "Locker object should not be null");
        assertEquals(lockerNumber, locker.getLockerNumber(), "Locker number should match");
        assertEquals(size, locker.getSize(), "Size should match");
        assertEquals(status, locker.getStatus(), "Status should match");
        assertEquals(hourlyRate, locker.getHourlyRate(), "Hourly rate should match");
        assertNotNull(locker.getBookings(), "Bookings list should be initialized");
    }

    // Test 3: Ten-parameter constructor (full constructor)
    @Test
    @DisplayName("Test 10-parameter constructor initializes all fields correctly")
    void testTenParameterConstructor() {
        String lockerNumber = "L002";
        Size size = Size.LARGE;
        Status status = Status.OCCUPIED;
        Double hourlyRate = 7.5;
        String locationName = "Central Station";
        String address = "123 Main St, City";
        Double latitude = 40.7128;
        Double longitude = -74.0060;
        String floor = "2";
        String section = "A";

        Locker locker = new Locker(lockerNumber, size, status, hourlyRate,
                locationName, address, latitude, longitude, floor, section);

        assertNotNull(locker, "Locker object should not be null");
        assertEquals(lockerNumber, locker.getLockerNumber(), "Locker number should match");
        assertEquals(size, locker.getSize(), "Size should match");
        assertEquals(status, locker.getStatus(), "Status should match");
        assertEquals(hourlyRate, locker.getHourlyRate(), "Hourly rate should match");
        assertEquals(locationName, locker.getLocationName(), "Location name should match");
        assertEquals(address, locker.getAddress(), "Address should match");
        assertEquals(latitude, locker.getLatitude(), "Latitude should match");
        assertEquals(longitude, locker.getLongitude(), "Longitude should match");
        assertEquals(floor, locker.getFloor(), "Floor should match");
        assertEquals(section, locker.getSection(), "Section should match");
    }

    // Test 4: Functionality - isAvailable() returns true for AVAILABLE status
    @Test
    @DisplayName("Test isAvailable() returns true when status is AVAILABLE")
    void testIsAvailable_WithAvailableStatus() {
        Locker locker = new Locker("L003", Size.SMALL, Status.AVAILABLE, 3.0);

        assertTrue(locker.isAvailable(), "isAvailable() should return true for AVAILABLE status");
    }

    // Test 5: Functionality - isAvailable() returns false for OCCUPIED status
    @Test
    @DisplayName("Test isAvailable() returns false when status is OCCUPIED")
    void testIsAvailable_WithOccupiedStatus() {
        Locker locker = new Locker("L004", Size.SMALL, Status.OCCUPIED, 3.0);

        assertFalse(locker.isAvailable(), "isAvailable() should return false for OCCUPIED status");
    }

    // Test 6: Functionality - updateStatus()
    @Test
    @DisplayName("Test updateStatus() changes locker status correctly")
    void testUpdateStatus() {
        Locker locker = new Locker("L005", Size.MEDIUM, Status.AVAILABLE, 5.0);

        assertEquals(Status.AVAILABLE, locker.getStatus(), "Initial status should be AVAILABLE");

        locker.updateStatus(Status.OCCUPIED);

        assertEquals(Status.OCCUPIED, locker.getStatus(), "Status should be updated to OCCUPIED");
    }

    // Test 7: Functionality - markAsAvailable()
    @Test
    @DisplayName("Test markAsAvailable() sets status to AVAILABLE")
    void testMarkAsAvailable() {
        Locker locker = new Locker("L006", Size.LARGE, Status.OCCUPIED, 7.0);

        assertEquals(Status.OCCUPIED, locker.getStatus(), "Initial status should be OCCUPIED");

        locker.markAsAvailable();

        assertEquals(Status.AVAILABLE, locker.getStatus(), "Status should be AVAILABLE after marking");
        assertTrue(locker.isAvailable(), "isAvailable() should return true");
    }

    // Test 8: Functionality - markAsOccupied()
    @Test
    @DisplayName("Test markAsOccupied() sets status to OCCUPIED")
    void testMarkAsOccupied() {
        Locker locker = new Locker("L007", Size.SMALL, Status.AVAILABLE, 3.0);

        assertEquals(Status.AVAILABLE, locker.getStatus(), "Initial status should be AVAILABLE");

        locker.markAsOccupied();

        assertEquals(Status.OCCUPIED, locker.getStatus(), "Status should be OCCUPIED after marking");
        assertFalse(locker.isAvailable(), "isAvailable() should return false");
    }

    // Test 9: Functionality - addBooking()
    @Test
    @DisplayName("Test addBooking() adds booking to locker's list")
    void testAddBooking() {
        Locker locker = new Locker("L008", Size.MEDIUM, Status.AVAILABLE, 5.0);
        Booking booking = new Booking();

        assertEquals(0, locker.getBookings().size(), "Initially, bookings should be empty");

        locker.addBooking(booking);

        assertEquals(1, locker.getBookings().size(), "After adding, bookings size should be 1");
        assertTrue(locker.getBookings().contains(booking), "Bookings list should contain the added booking");
        assertEquals(locker, booking.getLocker(), "Booking's locker should be set to this locker");
    }

    // Test 10: Functionality - removeBooking()
    @Test
    @DisplayName("Test removeBooking() removes booking from locker's list")
    void testRemoveBooking() {
        Locker locker = new Locker("L009", Size.LARGE, Status.AVAILABLE, 7.0);
        Booking booking = new Booking();

        locker.addBooking(booking);
        assertEquals(1, locker.getBookings().size(), "After adding, bookings size should be 1");

        locker.removeBooking(booking);

        assertEquals(0, locker.getBookings().size(), "After removing, bookings should be empty");
        assertFalse(locker.getBookings().contains(booking), "Bookings list should not contain the removed booking");
        assertNull(booking.getLocker(), "Booking's locker should be set to null");
    }

    // Test 11: Functionality - equals() method
    @Test
    @DisplayName("Test equals() method compares lockers correctly")
    void testEquals() {
        Locker locker1 = new Locker("L010", Size.SMALL, Status.AVAILABLE, 3.0);
        locker1.setId(1L);

        Locker locker2 = new Locker("L010", Size.SMALL, Status.AVAILABLE, 3.0);
        locker2.setId(1L);

        Locker locker3 = new Locker("L011", Size.MEDIUM, Status.OCCUPIED, 5.0);
        locker3.setId(2L);

        assertEquals(locker1, locker2, "Lockers with same ID and locker number should be equal");
        assertNotEquals(locker1, locker3, "Lockers with different ID should not be equal");
        assertNotEquals(locker1, null, "Locker should not equal null");
        assertEquals(locker1, locker1, "Locker should equal itself");
    }

    // Test 12: Functionality - hashCode() method
    @Test
    @DisplayName("Test hashCode() returns consistent values")
    void testHashCode() {
        Locker locker1 = new Locker("L012", Size.SMALL, Status.AVAILABLE, 3.0);
        locker1.setId(1L);

        Locker locker2 = new Locker("L012", Size.SMALL, Status.AVAILABLE, 3.0);
        locker2.setId(1L);

        assertEquals(locker1.hashCode(), locker2.hashCode(),
                "Equal lockers should have the same hash code");
    }

    // Test 13: Functionality - test all setters
    @Test
    @DisplayName("Test all setters modify fields correctly")
    void testSetters() {
        Locker locker = new Locker();

        locker.setId(100L);
        locker.setLockerNumber("L999");
        locker.setSize(Size.LARGE);
        locker.setStatus(Status.OCCUPIED);
        locker.setHourlyRate(10.0);
        locker.setLocationName("Airport Terminal");
        locker.setAddress("456 Airport Rd");
        locker.setLatitude(51.5074);
        locker.setLongitude(-0.1278);
        locker.setFloor("3");
        locker.setSection("B");
        locker.setVersion(5L);

        assertEquals(100L, locker.getId(), "ID should be updated");
        assertEquals("L999", locker.getLockerNumber(), "Locker number should be updated");
        assertEquals(Size.LARGE, locker.getSize(), "Size should be updated");
        assertEquals(Status.OCCUPIED, locker.getStatus(), "Status should be updated");
        assertEquals(10.0, locker.getHourlyRate(), "Hourly rate should be updated");
        assertEquals("Airport Terminal", locker.getLocationName(), "Location name should be updated");
        assertEquals("456 Airport Rd", locker.getAddress(), "Address should be updated");
        assertEquals(51.5074, locker.getLatitude(), "Latitude should be updated");
        assertEquals(-0.1278, locker.getLongitude(), "Longitude should be updated");
        assertEquals("3", locker.getFloor(), "Floor should be updated");
        assertEquals("B", locker.getSection(), "Section should be updated");
        assertEquals(5L, locker.getVersion(), "Version should be updated");
    }

    // Test 14: Functionality - toString() method
    @Test
    @DisplayName("Test toString() returns proper string representation")
    void testToString() {
        Locker locker = new Locker("L013", Size.MEDIUM, Status.AVAILABLE, 5.0,
                "Downtown", "789 Main St", 48.8566, 2.3522, "1", "C");
        locker.setId(1L);

        String result = locker.toString();

        assertNotNull(result, "toString() should not return null");
        assertTrue(result.contains("Locker{"), "toString() should contain class name");
        assertTrue(result.contains("id=1"), "toString() should contain ID");
        assertTrue(result.contains("lockerNumber='L013'"), "toString() should contain locker number");
        assertTrue(result.contains("size=MEDIUM"), "toString() should contain size");
        assertTrue(result.contains("status=AVAILABLE"), "toString() should contain status");
        assertTrue(result.contains("hourlyRate=5.0"), "toString() should contain hourly rate");
        assertTrue(result.contains("locationName='Downtown'"), "toString() should contain location name");
    }

    // Test 15: Validation - test hourly rate validation concept
    @Test
    @DisplayName("Test hourly rate can be set to valid positive value")
    void testHourlyRateValidation_PositiveValue() {
        Locker locker = new Locker("L014", Size.SMALL, Status.AVAILABLE, 2.5);

        assertEquals(2.5, locker.getHourlyRate(), "Positive hourly rate should be accepted");
        assertTrue(locker.getHourlyRate() > 0, "Hourly rate should be positive");
    }

    // Test 16: Validation - test zero hourly rate
    @Test
    @DisplayName("Test hourly rate can be set to zero")
    void testHourlyRateValidation_ZeroValue() {
        Locker locker = new Locker("L015", Size.SMALL, Status.AVAILABLE, 0.0);

        assertEquals(0.0, locker.getHourlyRate(), "Zero hourly rate should be accepted (edge case)");
    }
}
