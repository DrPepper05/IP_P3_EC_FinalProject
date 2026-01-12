package com.luggagestorage.service;

import com.luggagestorage.exception.ResourceNotFoundException;
import com.luggagestorage.model.Locker;
import com.luggagestorage.model.enums.Size;
import com.luggagestorage.model.enums.Status;
import com.luggagestorage.repository.LockerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Locker entity.
 * Provides CRUD operations, availability checking, and business logic for locker management.
 */
@Service
@Transactional
public class LockerService {

    private static final Logger logger = LoggerFactory.getLogger(LockerService.class);
    private static final String LOCKERS_FILE = "lockers.json";

    private final LockerRepository lockerRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public LockerService(LockerRepository lockerRepository, FileStorageService fileStorageService) {
        this.lockerRepository = lockerRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Create a new locker.
     *
     * @param locker The locker to create
     * @return The created locker
     */
    public Locker createLocker(Locker locker) {
        logger.info("Creating new locker with number: {}", locker.getLockerNumber());

        // Check if locker number already exists
        if (lockerRepository.existsByLockerNumber(locker.getLockerNumber())) {
            throw new IllegalArgumentException("Locker number already exists: " + locker.getLockerNumber());
        }

        // Validate hourly rate
        if (locker.getHourlyRate() == null || locker.getHourlyRate() < 0) {
            throw new IllegalArgumentException("Hourly rate must be positive");
        }

        Locker savedLocker = lockerRepository.save(locker);
        saveToFile();
        logger.info("Locker created successfully with ID: {}", savedLocker.getId());
        return savedLocker;
    }

    /**
     * Get a locker by ID.
     *
     * @param id The locker ID
     * @return The locker
     * @throws ResourceNotFoundException if locker not found
     */
    public Locker getLockerById(Long id) {
        return lockerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Locker", "id", id));
    }

    /**
     * Get a locker by locker number.
     *
     * @param lockerNumber The locker number
     * @return Optional containing the locker if found
     */
    public Optional<Locker> getLockerByNumber(String lockerNumber) {
        return lockerRepository.findByLockerNumber(lockerNumber);
    }

    /**
     * Get all lockers.
     *
     * @return List of all lockers
     */
    public List<Locker> getAllLockers() {
        return lockerRepository.findAll();
    }

    /**
     * Get all available lockers (no active bookings).
     * This is the primary method for customers to browse available lockers.
     * Now properly checks for active bookings, not just the status field.
     *
     * @return List of truly available lockers
     */
    public List<Locker> getAvailableLockers() {
        return lockerRepository.findTrulyAvailableLockers();
    }

    /**
     * Get lockers available for a specific time range.
     * Checks that lockers have no overlapping active bookings in the specified period.
     *
     * @param startTime Start of the desired booking period
     * @param endTime End of the desired booking period
     * @return List of lockers available for the time range
     */
    public List<Locker> getAvailableLockers(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            // Fall back to current availability if no time range specified
            return getAvailableLockers();
        }
        return lockerRepository.findAvailableForTimeRange(startTime, endTime);
    }

    /**
     * Get all lockers by size.
     *
     * @param size The size to filter by
     * @return List of lockers with the specified size
     */
    public List<Locker> getLockersBySize(Size size) {
        return lockerRepository.findBySize(size);
    }

    /**
     * Get available lockers by size.
     *
     * @param size The size to filter by
     * @return List of available lockers with the specified size
     */
    public List<Locker> getAvailableLockersBySize(Size size) {
        return lockerRepository.findBySizeAndStatus(size, Status.AVAILABLE);
    }

    /**
     * Get all lockers by status.
     *
     * @param status The status to filter by
     * @return List of lockers with the specified status
     */
    public List<Locker> getLockersByStatus(Status status) {
        return lockerRepository.findByStatus(status);
    }

    /**
     * Check if a locker is available.
     * Uses the isAvailable() method from the Locker entity.
     *
     * @param lockerId The locker ID
     * @return true if available, false otherwise
     */
    public boolean isLockerAvailable(Long lockerId) {
        Locker locker = getLockerById(lockerId);
        return locker.isAvailable();
    }

    /**
     * Update a locker.
     *
     * @param id     The locker ID
     * @param locker The updated locker data
     * @return The updated locker
     * @throws ResourceNotFoundException if locker not found
     */
    public Locker updateLocker(Long id, Locker locker) {
        logger.info("Updating locker with ID: {}", id);

        Locker existingLocker = getLockerById(id);

        // Update fields
        if (locker.getLockerNumber() != null && !locker.getLockerNumber().equals(existingLocker.getLockerNumber())) {
            // Check if new locker number already exists
            if (lockerRepository.existsByLockerNumber(locker.getLockerNumber())) {
                throw new IllegalArgumentException("Locker number already exists: " + locker.getLockerNumber());
            }
            existingLocker.setLockerNumber(locker.getLockerNumber());
        }

        if (locker.getSize() != null) {
            existingLocker.setSize(locker.getSize());
        }

        if (locker.getStatus() != null) {
            existingLocker.setStatus(locker.getStatus());
        }

        if (locker.getHourlyRate() != null) {
            if (locker.getHourlyRate() < 0) {
                throw new IllegalArgumentException("Hourly rate must be positive");
            }
            existingLocker.setHourlyRate(locker.getHourlyRate());
        }

        // Update location fields
        if (locker.getLocationName() != null) {
            existingLocker.setLocationName(locker.getLocationName());
        }

        if (locker.getAddress() != null) {
            existingLocker.setAddress(locker.getAddress());
        }

        if (locker.getLatitude() != null) {
            existingLocker.setLatitude(locker.getLatitude());
        }

        if (locker.getLongitude() != null) {
            existingLocker.setLongitude(locker.getLongitude());
        }

        if (locker.getFloor() != null) {
            existingLocker.setFloor(locker.getFloor());
        }

        if (locker.getSection() != null) {
            existingLocker.setSection(locker.getSection());
        }

        Locker updatedLocker = lockerRepository.save(existingLocker);
        saveToFile();
        logger.info("Locker updated successfully with ID: {}", updatedLocker.getId());
        return updatedLocker;
    }

    /**
     * Update locker status.
     * Uses the updateStatus() method from the Locker entity.
     *
     * @param lockerId The locker ID
     * @param status   The new status
     * @return The updated locker
     */
    public Locker updateLockerStatus(Long lockerId, Status status) {
        logger.info("Updating locker status for ID: {} to {}", lockerId, status);

        Locker locker = getLockerById(lockerId);
        locker.updateStatus(status);
        Locker updatedLocker = lockerRepository.save(locker);
        saveToFile();
        logger.info("Locker status updated successfully");
        return updatedLocker;
    }

    /**
     * Mark a locker as available.
     *
     * @param lockerId The locker ID
     * @return The updated locker
     */
    public Locker markAsAvailable(Long lockerId) {
        return updateLockerStatus(lockerId, Status.AVAILABLE);
    }

    /**
     * Mark a locker as occupied.
     *
     * @param lockerId The locker ID
     * @return The updated locker
     */
    public Locker markAsOccupied(Long lockerId) {
        return updateLockerStatus(lockerId, Status.OCCUPIED);
    }

    /**
     * Delete a locker by ID.
     *
     * @param id The locker ID
     * @throws ResourceNotFoundException if locker not found
     */
    public void deleteLocker(Long id) {
        logger.info("Deleting locker with ID: {}", id);

        Locker locker = getLockerById(id);

        // Check if locker has active bookings
        if (!locker.getBookings().isEmpty()) {
            logger.warn("Cannot delete locker with ID {} - has existing bookings", id);
            throw new IllegalArgumentException("Cannot delete locker with existing bookings");
        }

        lockerRepository.delete(locker);
        saveToFile();
        logger.info("Locker deleted successfully with ID: {}", id);
    }

    /**
     * Check if a locker exists by locker number.
     *
     * @param lockerNumber The locker number
     * @return true if exists, false otherwise
     */
    public boolean existsByLockerNumber(String lockerNumber) {
        return lockerRepository.existsByLockerNumber(lockerNumber);
    }

    /**
     * Save all lockers to JSON file.
     * Part of the file I/O requirement.
     */
    private void saveToFile() {
        try {
            List<Locker> lockers = lockerRepository.findAll();
            fileStorageService.saveToFile(lockers, LOCKERS_FILE);
        } catch (IOException e) {
            logger.error("Failed to save lockers to file: {}", e.getMessage());
            // Don't throw exception - file storage is supplementary
        }
    }

    /**
     * Load lockers from JSON file.
     * Part of the file I/O requirement.
     */
    public void loadFromFile() {
        try {
            if (fileStorageService.fileExists(LOCKERS_FILE)) {
                List<Locker> lockers = fileStorageService.loadFromFile(LOCKERS_FILE, Locker.class);
                logger.info("Loaded {} lockers from file", lockers.size());
            }
        } catch (IOException e) {
            logger.error("Failed to load lockers from file: {}", e.getMessage());
        }
    }
}
