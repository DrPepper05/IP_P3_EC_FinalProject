package com.luggagestorage.controller;

import com.luggagestorage.model.Locker;
import com.luggagestorage.model.dto.LockerRequest;
import com.luggagestorage.model.enums.Size;
import com.luggagestorage.model.enums.Status;
import com.luggagestorage.service.LockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Locker management endpoints.
 * GET endpoints are public (customers can browse available lockers).
 * POST, PUT, DELETE endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/api/lockers")
@CrossOrigin(origins = "*")
public class LockerController {

    private final LockerService lockerService;

    @Autowired
    public LockerController(LockerService lockerService) {
        this.lockerService = lockerService;
    }

    /**
     * Get all lockers (simple list view).
     * GET /api/lockers
     * Public endpoint
     *
     * @return List of all lockers
     */
    @GetMapping
    public ResponseEntity<List<Locker>> getAllLockers() {
        List<Locker> lockers = lockerService.getAllLockers();
        return ResponseEntity.ok(lockers);
    }

    /**
     * Get all available lockers.
     * GET /api/lockers/available
     * Public endpoint - primary endpoint for customers to browse
     *
     * @return List of available lockers
     */
    @GetMapping("/available")
    public ResponseEntity<List<Locker>> getAvailableLockers() {
        List<Locker> lockers = lockerService.getAvailableLockers();
        return ResponseEntity.ok(lockers);
    }

    /**
     * Get lockers available for a specific time range.
     * GET /api/lockers/available/time-range
     * Public endpoint - allows customers to check availability for future bookings
     *
     * @param startTime The start datetime (ISO format)
     * @param endTime The end datetime (ISO format)
     * @return List of lockers available during the specified time range
     */
    @GetMapping("/available/time-range")
    public ResponseEntity<List<Locker>> getAvailableLockersForTimeRange(
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime) {

        // Parse the datetime strings
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        // Validate that start is before end
        if (start.isAfter(end) || start.isEqual(end)) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Locker> lockers = lockerService.getAvailableLockers(start, end);
        return ResponseEntity.ok(lockers);
    }

    /**
     * Get a locker by ID.
     * GET /api/lockers/{id}
     * Public endpoint
     *
     * @param id The locker ID
     * @return The locker
     */
    @GetMapping("/{id}")
    public ResponseEntity<Locker> getLockerById(@PathVariable Long id) {
        Locker locker = lockerService.getLockerById(id);
        return ResponseEntity.ok(locker);
    }

    /**
     * Get lockers by size.
     * GET /api/lockers/size/{size}
     * Public endpoint
     *
     * @param size The locker size
     * @return List of lockers with the specified size
     */
    @GetMapping("/size/{size}")
    public ResponseEntity<List<Locker>> getLockersBySize(@PathVariable Size size) {
        List<Locker> lockers = lockerService.getLockersBySize(size);
        return ResponseEntity.ok(lockers);
    }

    /**
     * Get available lockers by size.
     * GET /api/lockers/available/size/{size}
     * Public endpoint
     *
     * @param size The locker size
     * @return List of available lockers with the specified size
     */
    @GetMapping("/available/size/{size}")
    public ResponseEntity<List<Locker>> getAvailableLockersBySize(@PathVariable Size size) {
        List<Locker> lockers = lockerService.getAvailableLockersBySize(size);
        return ResponseEntity.ok(lockers);
    }

    /**
     * Get lockers by status.
     * GET /api/lockers/status/{status}
     * Public endpoint
     *
     * @param status The locker status
     * @return List of lockers with the specified status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Locker>> getLockersByStatus(@PathVariable Status status) {
        List<Locker> lockers = lockerService.getLockersByStatus(status);
        return ResponseEntity.ok(lockers);
    }

    /**
     * Create a new locker.
     * POST /api/lockers
     * Admin only
     *
     * @param lockerRequest The locker creation request
     * @return The created locker
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Locker> createLocker(@Valid @RequestBody LockerRequest lockerRequest) {
        Locker locker = new Locker(
                lockerRequest.getLockerNumber(),
                lockerRequest.getSize(),
                lockerRequest.getStatus(),
                lockerRequest.getHourlyRate(),
                lockerRequest.getLocationName(),
                lockerRequest.getAddress(),
                lockerRequest.getLatitude(),
                lockerRequest.getLongitude(),
                lockerRequest.getFloor(),
                lockerRequest.getSection()
        );
        Locker createdLocker = lockerService.createLocker(locker);
        return new ResponseEntity<>(createdLocker, HttpStatus.CREATED);
    }

    /**
     * Update a locker.
     * PUT /api/lockers/{id}
     * Admin only
     *
     * @param id            The locker ID
     * @param lockerRequest The updated locker data
     * @return The updated locker
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Locker> updateLocker(@PathVariable Long id,
                                                @Valid @RequestBody LockerRequest lockerRequest) {
        Locker locker = new Locker(
                lockerRequest.getLockerNumber(),
                lockerRequest.getSize(),
                lockerRequest.getStatus(),
                lockerRequest.getHourlyRate(),
                lockerRequest.getLocationName(),
                lockerRequest.getAddress(),
                lockerRequest.getLatitude(),
                lockerRequest.getLongitude(),
                lockerRequest.getFloor(),
                lockerRequest.getSection()
        );
        Locker updatedLocker = lockerService.updateLocker(id, locker);
        return ResponseEntity.ok(updatedLocker);
    }

    /**
     * Update locker status.
     * PUT /api/lockers/{id}/status
     * Admin only
     *
     * @param id     The locker ID
     * @param status The new status
     * @return The updated locker
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Locker> updateLockerStatus(@PathVariable Long id, @RequestParam Status status) {
        Locker updatedLocker = lockerService.updateLockerStatus(id, status);
        return ResponseEntity.ok(updatedLocker);
    }

    /**
     * Get all lockers grouped by location.
     * GET /api/lockers/by-location
     * Public endpoint
     *
     * @return Map of lockers grouped by location name
     */
    @GetMapping("/by-location")
    public ResponseEntity<Map<String, List<Locker>>> getLockersByLocation() {
        List<Locker> allLockers = lockerService.getAllLockers();
        Map<String, List<Locker>> byLocation = allLockers.stream()
                .filter(locker -> locker.getLocationName() != null)
                .collect(Collectors.groupingBy(Locker::getLocationName));
        return ResponseEntity.ok(byLocation);
    }

    /**
     * Get locker statistics.
     * GET /api/lockers/statistics
     * Public endpoint
     *
     * @return Statistics about locker counts by status
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        List<Locker> allLockers = lockerService.getAllLockers();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", allLockers.size());
        stats.put("available", allLockers.stream().filter(l -> l.getStatus() == Status.AVAILABLE).count());
        stats.put("occupied", allLockers.stream().filter(l -> l.getStatus() == Status.OCCUPIED).count());
        stats.put("reserved", allLockers.stream().filter(l -> l.getStatus() == Status.RESERVED).count());
        stats.put("maintenance", allLockers.stream().filter(l -> l.getStatus() == Status.MAINTENANCE).count());
        stats.put("outOfOrder", allLockers.stream().filter(l -> l.getStatus() == Status.OUT_OF_ORDER).count());

        long available = (long) stats.get("available");
        double availabilityRate = allLockers.size() > 0 ?
                (available * 100.0 / allLockers.size()) : 0.0;
        stats.put("availabilityRate", String.format("%.1f", availabilityRate));

        return ResponseEntity.ok(stats);
    }

    /**
     * Get lockers within radius from a point.
     * GET /api/lockers/nearby?latitude=X&longitude=Y&radiusKm=Z
     * Public endpoint
     *
     * @param latitude  The center point latitude
     * @param longitude The center point longitude
     * @param radiusKm  The radius in kilometers (default 5km)
     * @return List of lockers within the specified radius
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<Locker>> getNearbyLockers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {

        List<Locker> allLockers = lockerService.getAllLockers();

        // Filter lockers within radius using Haversine formula
        List<Locker> nearbyLockers = allLockers.stream()
                .filter(locker -> locker.getLatitude() != null && locker.getLongitude() != null)
                .filter(locker -> {
                    double distance = calculateDistance(latitude, longitude,
                            locker.getLatitude(), locker.getLongitude());
                    return distance <= radiusKm;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(nearbyLockers);
    }

    /**
     * Calculate distance between two points using Haversine formula.
     *
     * @param lat1 First point latitude
     * @param lon1 First point longitude
     * @param lat2 Second point latitude
     * @param lon2 Second point longitude
     * @return Distance in kilometers
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Delete a locker.
     * DELETE /api/lockers/{id}
     * Admin only
     *
     * @param id The locker ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocker(@PathVariable Long id) {
        lockerService.deleteLocker(id);
        return ResponseEntity.noContent().build();
    }
}
