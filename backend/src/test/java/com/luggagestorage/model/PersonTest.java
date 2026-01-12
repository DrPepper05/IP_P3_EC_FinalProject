package com.luggagestorage.model;

import com.luggagestorage.model.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Person entity.
 * Tests constructors and functionality as required by the project requirements.
 */
class PersonTest {

    // Test 1: Default constructor
    @Test
    @DisplayName("Test default constructor creates empty Person")
    void testDefaultConstructor() {
        Person person = new Person();

        assertNotNull(person, "Person object should not be null");
        assertNull(person.getId(), "ID should be null for new person");
        assertNull(person.getEmail(), "Email should be null");
        assertNull(person.getPasswordHash(), "Password hash should be null");
        assertNull(person.getFirstName(), "First name should be null");
        assertNull(person.getLastName(), "Last name should be null");
        assertNull(person.getRole(), "Role should be null");
        assertNotNull(person.getBookings(), "Bookings list should be initialized");
        assertTrue(person.getBookings().isEmpty(), "Bookings list should be empty");
    }

    // Test 2: Parameterized constructor
    @Test
    @DisplayName("Test parameterized constructor initializes all fields correctly")
    void testParameterizedConstructor() {
        String email = "john.doe@example.com";
        String passwordHash = "hashedPassword123";
        String firstName = "John";
        String lastName = "Doe";
        Role role = Role.CUSTOMER;

        Person person = new Person(email, passwordHash, firstName, lastName, role);

        assertNotNull(person, "Person object should not be null");
        assertEquals(email, person.getEmail(), "Email should match");
        assertEquals(passwordHash, person.getPasswordHash(), "Password hash should match");
        assertEquals(firstName, person.getFirstName(), "First name should match");
        assertEquals(lastName, person.getLastName(), "Last name should match");
        assertEquals(role, person.getRole(), "Role should match");
        assertNotNull(person.getBookings(), "Bookings list should be initialized");
    }

    // Test 3: Functionality - getFullName()
    @Test
    @DisplayName("Test getFullName() returns correct full name")
    void testGetFullName() {
        Person person = new Person("test@example.com", "pass", "Jane", "Smith", Role.ADMIN);

        String fullName = person.getFullName();

        assertEquals("Jane Smith", fullName, "Full name should be firstName + space + lastName");
    }

    // Test 4: Functionality - isAdmin() for admin role
    @Test
    @DisplayName("Test isAdmin() returns true for ADMIN role")
    void testIsAdmin_WithAdminRole() {
        Person admin = new Person("admin@example.com", "pass", "Admin", "User", Role.ADMIN);

        assertTrue(admin.isAdmin(), "isAdmin() should return true for ADMIN role");
        assertFalse(admin.isCustomer(), "isCustomer() should return false for ADMIN role");
    }

    // Test 5: Functionality - isCustomer() for customer role
    @Test
    @DisplayName("Test isCustomer() returns true for CUSTOMER role")
    void testIsCustomer_WithCustomerRole() {
        Person customer = new Person("customer@example.com", "pass", "Customer", "User", Role.CUSTOMER);

        assertTrue(customer.isCustomer(), "isCustomer() should return true for CUSTOMER role");
        assertFalse(customer.isAdmin(), "isAdmin() should return false for CUSTOMER role");
    }

    // Test 6: Functionality - addBooking()
    @Test
    @DisplayName("Test addBooking() adds booking to person's list")
    void testAddBooking() {
        Person person = new Person("test@example.com", "pass", "Test", "User", Role.CUSTOMER);
        Booking booking = new Booking();

        assertEquals(0, person.getBookings().size(), "Initially, bookings should be empty");

        person.addBooking(booking);

        assertEquals(1, person.getBookings().size(), "After adding, bookings size should be 1");
        assertTrue(person.getBookings().contains(booking), "Bookings list should contain the added booking");
        assertEquals(person, booking.getCustomer(), "Booking's customer should be set to this person");
    }

    // Test 7: Functionality - removeBooking()
    @Test
    @DisplayName("Test removeBooking() removes booking from person's list")
    void testRemoveBooking() {
        Person person = new Person("test@example.com", "pass", "Test", "User", Role.CUSTOMER);
        Booking booking = new Booking();

        person.addBooking(booking);
        assertEquals(1, person.getBookings().size(), "After adding, bookings size should be 1");

        person.removeBooking(booking);

        assertEquals(0, person.getBookings().size(), "After removing, bookings should be empty");
        assertFalse(person.getBookings().contains(booking), "Bookings list should not contain the removed booking");
        assertNull(booking.getCustomer(), "Booking's customer should be set to null");
    }

    // Test 8: Functionality - equals() method
    @Test
    @DisplayName("Test equals() method compares persons correctly")
    void testEquals() {
        Person person1 = new Person("test@example.com", "pass", "Test", "User", Role.CUSTOMER);
        person1.setId(1L);

        Person person2 = new Person("test@example.com", "pass", "Test", "User", Role.CUSTOMER);
        person2.setId(1L);

        Person person3 = new Person("other@example.com", "pass", "Other", "User", Role.CUSTOMER);
        person3.setId(2L);

        assertEquals(person1, person2, "Persons with same ID and email should be equal");
        assertNotEquals(person1, person3, "Persons with different ID should not be equal");
        assertNotEquals(person1, null, "Person should not equal null");
        assertEquals(person1, person1, "Person should equal itself");
    }

    // Test 9: Functionality - hashCode() method
    @Test
    @DisplayName("Test hashCode() returns consistent values")
    void testHashCode() {
        Person person1 = new Person("test@example.com", "pass", "Test", "User", Role.CUSTOMER);
        person1.setId(1L);

        Person person2 = new Person("test@example.com", "pass", "Test", "User", Role.CUSTOMER);
        person2.setId(1L);

        assertEquals(person1.hashCode(), person2.hashCode(),
            "Equal persons should have the same hash code");
    }

    // Test 10: Functionality - setters work correctly
    @Test
    @DisplayName("Test all setters modify fields correctly")
    void testSetters() {
        Person person = new Person();

        person.setId(100L);
        person.setEmail("updated@example.com");
        person.setPasswordHash("newHash");
        person.setFirstName("Updated");
        person.setLastName("Name");
        person.setRole(Role.ADMIN);

        assertEquals(100L, person.getId(), "ID should be updated");
        assertEquals("updated@example.com", person.getEmail(), "Email should be updated");
        assertEquals("newHash", person.getPasswordHash(), "Password hash should be updated");
        assertEquals("Updated", person.getFirstName(), "First name should be updated");
        assertEquals("Name", person.getLastName(), "Last name should be updated");
        assertEquals(Role.ADMIN, person.getRole(), "Role should be updated");
    }

    // Test 11: Functionality - toString() method
    @Test
    @DisplayName("Test toString() returns proper string representation")
    void testToString() {
        Person person = new Person("test@example.com", "pass", "Test", "User", Role.CUSTOMER);
        person.setId(1L);

        String result = person.toString();

        assertNotNull(result, "toString() should not return null");
        assertTrue(result.contains("Person{"), "toString() should contain class name");
        assertTrue(result.contains("id=1"), "toString() should contain ID");
        assertTrue(result.contains("email='test@example.com'"), "toString() should contain email");
        assertTrue(result.contains("firstName='Test'"), "toString() should contain first name");
        assertTrue(result.contains("lastName='User'"), "toString() should contain last name");
        assertTrue(result.contains("role=CUSTOMER"), "toString() should contain role");
    }
}
