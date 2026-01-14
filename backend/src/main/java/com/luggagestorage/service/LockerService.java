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

    public Locker createLocker(Locker locker) {
        logger.info("Creating new locker with number: {}", locker.getLockerNumber());

        if (lockerRepository.existsByLockerNumber(locker.getLockerNumber())) {
            throw new IllegalArgumentException("Locker number already exists: " + locker.getLockerNumber());
        }

        if (locker.getHourlyRate() == null || locker.getHourlyRate() < 0) {
            throw new IllegalArgumentException("Hourly rate must be positive");
        }

        Locker savedLocker = lockerRepository.save(locker);
        saveToFile();
        logger.info("Locker created successfully with ID: {}", savedLocker.getId());
        return savedLocker;
    }

    public Locker getLockerById(Long id) {
        return lockerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Locker", "id", id));
    }

    public Optional<Locker> getLockerByNumber(String lockerNumber) {
        return lockerRepository.findByLockerNumber(lockerNumber);
    }

    public List<Locker> getAllLockers() {
        return lockerRepository.findAll();
    }

    public List<Locker> getAvailableLockers() {
        return lockerRepository.findTrulyAvailableLockers();
    }

    public List<Locker> getAvailableLockers(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {

            return getAvailableLockers();
        }
        return lockerRepository.findAvailableForTimeRange(startTime, endTime);
    }

    public List<Locker> getLockersBySize(Size size) {
        return lockerRepository.findBySize(size);
    }

    public List<Locker> getAvailableLockersBySize(Size size) {
        return lockerRepository.findBySizeAndStatus(size, Status.AVAILABLE);
    }

    public List<Locker> getLockersByStatus(Status status) {
        return lockerRepository.findByStatus(status);
    }

    public boolean isLockerAvailable(Long lockerId) {
        Locker locker = getLockerById(lockerId);
        return locker.isAvailable();
    }

    public Locker updateLocker(Long id, Locker locker) {
        logger.info("Updating locker with ID: {}", id);

        Locker existingLocker = getLockerById(id);

        if (locker.getLockerNumber() != null && !locker.getLockerNumber().equals(existingLocker.getLockerNumber())) {

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

    public Locker updateLockerStatus(Long lockerId, Status status) {
        logger.info("Updating locker status for ID: {} to {}", lockerId, status);

        Locker locker = getLockerById(lockerId);
        locker.updateStatus(status);
        Locker updatedLocker = lockerRepository.save(locker);
        saveToFile();
        logger.info("Locker status updated successfully");
        return updatedLocker;
    }

    public Locker markAsAvailable(Long lockerId) {
        return updateLockerStatus(lockerId, Status.AVAILABLE);
    }

    public Locker markAsOccupied(Long lockerId) {
        return updateLockerStatus(lockerId, Status.OCCUPIED);
    }

    public void deleteLocker(Long id) {
        logger.info("Deleting locker with ID: {}", id);

        Locker locker = getLockerById(id);

        if (!locker.getBookings().isEmpty()) {
            logger.warn("Cannot delete locker with ID {} - has existing bookings", id);
            throw new IllegalArgumentException("Cannot delete locker with existing bookings");
        }

        lockerRepository.delete(locker);
        saveToFile();
        logger.info("Locker deleted successfully with ID: {}", id);
    }

    public boolean existsByLockerNumber(String lockerNumber) {
        return lockerRepository.existsByLockerNumber(lockerNumber);
    }

    private void saveToFile() {
        try {
            List<Locker> lockers = lockerRepository.findAll();
            fileStorageService.saveToFile(lockers, LOCKERS_FILE);
        } catch (IOException e) {
            logger.error("Failed to save lockers to file: {}", e.getMessage());

        }
    }

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
