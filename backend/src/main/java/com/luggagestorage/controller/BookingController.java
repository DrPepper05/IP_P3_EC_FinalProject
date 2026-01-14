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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);

        Person currentUser = authService.getCurrentUser();
        if (!currentUser.isAdmin() && !booking.getCustomer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(convertToResponse(booking));
    }

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

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByCustomer(@PathVariable Long customerId) {
        List<Booking> bookings = bookingService.getBookingsByCustomer(customerId);
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/locker/{lockerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByLocker(@PathVariable Long lockerId) {
        List<Booking> bookings = bookingService.getBookingsByLocker(lockerId);
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getActiveBookings() {
        List<Booking> bookings = bookingService.getActiveBookings();
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(@PathVariable BookingStatus status) {
        List<Booking> bookings = bookingService.getBookingsByStatus(status);
        List<BookingResponse> responses = bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

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

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long id,
                                                          @Valid @RequestBody BookingRequest bookingRequest) {
        Booking existingBooking = bookingService.getBookingById(id);

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

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        Booking existingBooking = bookingService.getBookingById(id);

        Person currentUser = authService.getCurrentUser();
        if (!currentUser.isAdmin() && !existingBooking.getCustomer().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Booking cancelledBooking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(convertToResponse(cancelledBooking));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable Long id) {
        Booking completedBooking = bookingService.completeBooking(id);
        return ResponseEntity.ok(convertToResponse(completedBooking));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

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
