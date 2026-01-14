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

@RestController
@RequestMapping("/api/lockers")
@CrossOrigin(origins = "*")
public class LockerController {

    private final LockerService lockerService;

    @Autowired
    public LockerController(LockerService lockerService) {
        this.lockerService = lockerService;
    }

    @GetMapping
    public ResponseEntity<List<Locker>> getAllLockers() {
        List<Locker> lockers = lockerService.getAllLockers();
        return ResponseEntity.ok(lockers);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Locker>> getAvailableLockers() {
        List<Locker> lockers = lockerService.getAvailableLockers();
        return ResponseEntity.ok(lockers);
    }

    @GetMapping("/available/time-range")
    public ResponseEntity<List<Locker>> getAvailableLockersForTimeRange(
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime) {

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);

        if (start.isAfter(end) || start.isEqual(end)) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Locker> lockers = lockerService.getAvailableLockers(start, end);
        return ResponseEntity.ok(lockers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Locker> getLockerById(@PathVariable Long id) {
        Locker locker = lockerService.getLockerById(id);
        return ResponseEntity.ok(locker);
    }

    @GetMapping("/size/{size}")
    public ResponseEntity<List<Locker>> getLockersBySize(@PathVariable Size size) {
        List<Locker> lockers = lockerService.getLockersBySize(size);
        return ResponseEntity.ok(lockers);
    }

    @GetMapping("/available/size/{size}")
    public ResponseEntity<List<Locker>> getAvailableLockersBySize(@PathVariable Size size) {
        List<Locker> lockers = lockerService.getAvailableLockersBySize(size);
        return ResponseEntity.ok(lockers);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Locker>> getLockersByStatus(@PathVariable Status status) {
        List<Locker> lockers = lockerService.getLockersByStatus(status);
        return ResponseEntity.ok(lockers);
    }

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

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Locker> updateLockerStatus(@PathVariable Long id, @RequestParam Status status) {
        Locker updatedLocker = lockerService.updateLockerStatus(id, status);
        return ResponseEntity.ok(updatedLocker);
    }

    @GetMapping("/by-location")
    public ResponseEntity<Map<String, List<Locker>>> getLockersByLocation() {
        List<Locker> allLockers = lockerService.getAllLockers();
        Map<String, List<Locker>> byLocation = allLockers.stream()
                .filter(locker -> locker.getLocationName() != null)
                .collect(Collectors.groupingBy(Locker::getLocationName));
        return ResponseEntity.ok(byLocation);
    }

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

    @GetMapping("/nearby")
    public ResponseEntity<List<Locker>> getNearbyLockers(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {

        List<Locker> allLockers = lockerService.getAllLockers();

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

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocker(@PathVariable Long id) {
        lockerService.deleteLocker(id);
        return ResponseEntity.noContent().build();
    }
}
