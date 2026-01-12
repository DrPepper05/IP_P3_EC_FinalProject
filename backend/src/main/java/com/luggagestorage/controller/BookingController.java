package com.luggagestorage.controller;

import com.luggagestorage.model.Booking;
import com.luggagestorage.model.Person;
import com.luggagestorage.model.dto.BookingRequest;
import com.luggagestorage.model.dto.BookingResponse;
import com.luggagestorage.model.enums.BookingStatus;
import com.luggagestorage.service.AuthService;
import com.luggagestorage.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Booking management endpoints.
 * Provides functionality for customers to make bookings and admins to manage all bookings.
 */
@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;
    private final AuthService authService;

    @Autowired
    public BookingController(BookingService bookingService, AuthService authService) {
        this.bookingService = bookingService;
        this.authService = authService;
    }

    /**
     * Get all bookings.
     * GET /api/bookings
     * Admin only
     *
     * @return List of all bookings
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get a booking by ID.
     * GET /api/bookings/{id}
     * Customer (own bookings) or Admin
     *
     * @param id The booking ID
     * @return The booking
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);

        // Check if customer is accessing their own booking
        Person currentUser = authService.getCurrentUser();
        if (!currentUser.isAdmin() && !booking.getCustomer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(convertToResponse(booking));
    }

    /**
     * Get all bookings for the current customer.
     * GET /api/bookings/my-bookings
     * Customer only
     *
     * @return List of customer's bookings
     */
    @GetMapping("/my-bookings")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        Person currentUser = authService.getCurrentUser();
        List<Booking> bookings = bookingService.getBookingsByCustomer(currentUser.getId());
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all active bookings for the current customer.
     * GET /api/bookings/my-bookings/active
     * Customer only
     *
     * @return List of customer's active bookings
     */
    @GetMapping("/my-bookings/active")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<BookingResponse>> getMyActiveBookings() {
        Person currentUser = authService.getCurrentUser();
        List<Booking> bookings = bookingService.getActiveBookingsByCustomer(currentUser.getId());
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all bookings for a specific customer.
     * GET /api/bookings/customer/{customerId}
     * Admin only
     *
     * @param customerId The customer ID
     * @return List of customer's bookings
     */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByCustomer(@PathVariable Long customerId) {
        List<Booking> bookings = bookingService.getBookingsByCustomer(customerId);
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all bookings for a specific locker.
     * GET /api/bookings/locker/{lockerId}
     * Admin only
     *
     * @param lockerId The locker ID
     * @return List of locker's bookings
     */
    @GetMapping("/locker/{lockerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByLocker(@PathVariable Long lockerId) {
        List<Booking> bookings = bookingService.getBookingsByLocker(lockerId);
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all active bookings.
     * GET /api/bookings/active
     * Admin only
     *
     * @return List of all active bookings
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getActiveBookings() {
        List<Booking> bookings = bookingService.getActiveBookings();
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get bookings by status.
     * GET /api/bookings/status/{status}
     * Admin only
     *
     * @param status The booking status
     * @return List of bookings with the specified status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(@PathVariable BookingStatus status) {
        List<Booking> bookings = bookingService.getBookingsByStatus(status);
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Create a new booking.
     * POST /api/bookings
     * Customer or Admin
     *
     * @param bookingRequest The booking creation request
     * @return The created booking
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        Person currentUser = authService.getCurrentUser();

        Booking booking = bookingService.createBooking(
                currentUser.getId(),
                bookingRequest.getLockerId(),
                bookingRequest.getStartDatetime(),
                bookingRequest.getEndDatetime()
        );

        return new ResponseEntity<>(convertToResponse(booking), HttpStatus.CREATED);
    }

    /**
     * Update a booking.
     * PUT /api/bookings/{id}
     * Customer (own bookings) or Admin
     *
     * @param id             The booking ID
     * @param bookingRequest The updated booking data
     * @return The updated booking
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long id,
                                                          @Valid @RequestBody BookingRequest bookingRequest) {
        Booking existingBooking = bookingService.getBookingById(id);

        // Check if customer is updating their own booking
        Person currentUser = authService.getCurrentUser();
        if (!currentUser.isAdmin() && !existingBooking.getCustomer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Booking updatedBooking = bookingService.updateBooking(
                id,
                bookingRequest.getStartDatetime(),
                bookingRequest.getEndDatetime()
        );

        return ResponseEntity.ok(convertToResponse(updatedBooking));
    }

    /**
     * Cancel a booking.
     * PUT /api/bookings/{id}/cancel
     * Customer (own bookings) or Admin
     *
     * @param id The booking ID
     * @return The cancelled booking
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        Booking existingBooking = bookingService.getBookingById(id);

        // Check if customer is cancelling their own booking
        Person currentUser = authService.getCurrentUser();
        if (!currentUser.isAdmin() && !existingBooking.getCustomer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Booking cancelledBooking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(convertToResponse(cancelledBooking));
    }

    /**
     * Complete a booking.
     * PUT /api/bookings/{id}/complete
     * Admin only
     *
     * @param id The booking ID
     * @return The completed booking
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable Long id) {
        Booking completedBooking = bookingService.completeBooking(id);
        return ResponseEntity.ok(convertToResponse(completedBooking));
    }

    /**
     * Delete a booking.
     * DELETE /api/bookings/{id}
     * Admin only
     *
     * @param id The booking ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Convert Booking entity to BookingResponse DTO.
     *
     * @param booking The booking entity
     * @return The booking response DTO
     */
    private BookingResponse convertToResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setCustomerId(booking.getCustomer().getId());
        response.setCustomerName(booking.getCustomer().getFullName());
        response.setLockerId(booking.getLocker().getId());
        response.setLockerNumber(booking.getLocker().getLockerNumber());
        response.setLockerSize(booking.getLocker().getSize());
        response.setStartDatetime(booking.getStartDatetime());
        response.setEndDatetime(booking.getEndDatetime());
        response.setStatus(booking.getStatus());
        response.setTotalPrice(booking.getTotalPrice());
        response.setDurationInHours(booking.getDurationInHours());
        return response;
    }
}
