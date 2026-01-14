package com.luggagestorage.socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luggagestorage.model.Booking;
import com.luggagestorage.model.Locker;
import com.luggagestorage.model.Person;
import com.luggagestorage.model.enums.Status;
import com.luggagestorage.service.BookingService;
import com.luggagestorage.service.LockerService;
import com.luggagestorage.service.PersonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SocketService {

    private static final Logger logger = LoggerFactory.getLogger(SocketService.class);

    private final BookingService bookingService;
    private final LockerService lockerService;
    private final PersonService personService;
    private final ObjectMapper objectMapper;

    @Autowired
    public SocketService(BookingService bookingService,
                         LockerService lockerService,
                         PersonService personService) {
        this.bookingService = bookingService;
        this.lockerService = lockerService;
        this.personService = personService;
        this.objectMapper = new ObjectMapper();
    }

    public String getSystemStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            status.put("status", "OK");
            status.put("service", "Luggage Storage System");
            status.put("version", "1.0.0");
            status.put("timestamp", System.currentTimeMillis());
            status.put("active", true);

            return objectMapper.writeValueAsString(status);
        } catch (JsonProcessingException e) {
            logger.error("Error creating system status JSON", e);
            return "{\"status\":\"ERROR\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    public String getSystemStatistics() {
        try {
            List<Booking> allBookings = bookingService.getAllBookings();
            List<Locker> allLockers = lockerService.getAllLockers();
            List<Person> allPersons = personService.getAllPersons();
            List<Booking> activeBookings = bookingService.getActiveBookings();
            List<Locker> availableLockers = lockerService.getAvailableLockers();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBookings", allBookings.size());
            stats.put("activeBookings", activeBookings.size());
            stats.put("totalLockers", allLockers.size());
            stats.put("availableLockers", availableLockers.size());
            stats.put("occupiedLockers", allLockers.size() - availableLockers.size());
            stats.put("totalUsers", allPersons.size());
            stats.put("timestamp", System.currentTimeMillis());

            return objectMapper.writeValueAsString(stats);
        } catch (Exception e) {
            logger.error("Error creating statistics JSON", e);
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    public String getAvailableLockers() {
        try {
            List<Locker> availableLockers = lockerService.getAvailableLockers();

            Map<String, Object> response = new HashMap<>();
            response.put("count", availableLockers.size());
            response.put("lockers", availableLockers.stream()
                    .map(locker -> {
                        Map<String, Object> lockerInfo = new HashMap<>();
                        lockerInfo.put("id", locker.getId());
                        lockerInfo.put("number", locker.getLockerNumber());
                        lockerInfo.put("size", locker.getSize().name());
                        lockerInfo.put("hourlyRate", locker.getHourlyRate());
                        lockerInfo.put("location", locker.getLocationName());
                        return lockerInfo;
                    })
                    .toList());

            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("Error creating available lockers JSON", e);
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    public String getActiveBookings() {
        try {
            List<Booking> activeBookings = bookingService.getActiveBookings();

            Map<String, Object> response = new HashMap<>();
            response.put("count", activeBookings.size());
            response.put("bookings", activeBookings.stream()
                    .map(booking -> {
                        Map<String, Object> bookingInfo = new HashMap<>();
                        bookingInfo.put("id", booking.getId());
                        bookingInfo.put("lockerId", booking.getLocker().getId());
                        bookingInfo.put("lockerNumber", booking.getLocker().getLockerNumber());
                        bookingInfo.put("customerId", booking.getCustomer().getId());
                        bookingInfo.put("customerName", booking.getCustomer().getFullName());
                        bookingInfo.put("startTime", booking.getStartDatetime().toString());
                        bookingInfo.put("endTime", booking.getEndDatetime().toString());
                        bookingInfo.put("totalPrice", booking.getTotalPrice());
                        return bookingInfo;
                    })
                    .toList());

            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            logger.error("Error creating active bookings JSON", e);
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    public String processCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return "{\"error\":\"Empty command\"}";
        }

        String cmd = command.trim().toUpperCase();
        logger.info("Processing socket command: {}", cmd);

        switch (cmd) {
            case "STATUS":
                return getSystemStatus();
            case "STATS":
            case "STATISTICS":
                return getSystemStatistics();
            case "LOCKERS":
                return getAvailableLockers();
            case "BOOKINGS":
                return getActiveBookings();
            case "HELP":
                return getHelpMessage();
            default:
                return "{\"error\":\"Unknown command: " + command + "\",\"hint\":\"Try HELP for available commands\"}";
        }
    }

    private String getHelpMessage() {
        try {
            Map<String, Object> help = new HashMap<>();
            help.put("service", "Luggage Storage System Socket Server");
            help.put("commands", List.of(
                    "STATUS - Get system status",
                    "STATS - Get system statistics",
                    "LOCKERS - Get available lockers",
                    "BOOKINGS - Get active bookings",
                    "HELP - Show this help message",
                    "QUIT - Close connection"
            ));

            return objectMapper.writeValueAsString(help);
        } catch (JsonProcessingException e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}
