package com.luggagestorage.config;

import com.luggagestorage.model.Booking;
import com.luggagestorage.model.Locker;
import com.luggagestorage.model.Person;
import com.luggagestorage.model.enums.BookingStatus;
import com.luggagestorage.model.enums.Role;
import com.luggagestorage.model.enums.Size;
import com.luggagestorage.model.enums.Status;
import com.luggagestorage.repository.BookingRepository;
import com.luggagestorage.repository.LockerRepository;
import com.luggagestorage.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * DataInitializer class that populates the database with realistic test data on startup.
 * Implements CommandLineRunner to run initialization logic after application startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private LockerRepository lockerRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.data.init.enabled:true}")
    private boolean dataInitEnabled;

    private final Random random = new Random();

    /**
     * Location data with coordinates and details
     */
    private static class LocationData {
        String name;
        String address;
        Double latitude;
        Double longitude;

        LocationData(String name, String address, Double latitude, Double longitude) {
            this.name = name;
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    @Override
    public void run(String... args) throws Exception {
        if (!dataInitEnabled) {
            return;
        }

        // Check if data already exists
        if (lockerRepository.count() > 0 || personRepository.count() > 0) {
            return;
        }

        System.out.println("Initializing database with test data...");

        // Initialize users
        List<Person> users = initializeUsers();

        // Initialize lockers
        List<Locker> lockers = initializeLockers();

        // Initialize bookings
        initializeBookings(users, lockers);

        System.out.println("Data initialization completed successfully!");
        System.out.println("Created " + lockerRepository.count() + " lockers, " +
                           personRepository.count() + " users, " +
                           bookingRepository.count() + " bookings");
    }

    /**
     * Initialize 17 users (2 admins + 15 customers)
     */
    private List<Person> initializeUsers() {
        Person admin1 = new Person(
            "admin@luggagestorage.com",
            passwordEncoder.encode("AdminPassword123!"),
            "John",
            "Administrator",
            Role.ADMIN
        );

        Person admin2 = new Person(
            "admin2@luggagestorage.com",
            passwordEncoder.encode("AdminPassword123!"),
            "Jane",
            "Manager",
            Role.ADMIN
        );

        // Customer names for diversity
        String[] firstNames = {"Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Henry", "Iris", "Jack", "Karen", "Liam", "Maria", "Nathan", "Olivia"};
        String[] lastNames = {"Smith", "Johnson", "Brown", "Davis", "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Garcia"};

        List<Person> users = new java.util.ArrayList<>();
        users.add(admin1);
        users.add(admin2);

        // Create 15 customer users
        for (int i = 0; i < 15; i++) {
            String firstName = firstNames[i];
            String lastName = lastNames[i];
            String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + "@example.com";
            Person customer = new Person(
                email,
                passwordEncoder.encode("CustomerPassword123!"),
                firstName,
                lastName,
                Role.CUSTOMER
            );
            users.add(customer);
        }

        personRepository.saveAll(users);
        return users;
    }

    /**
     * Initialize 130 lockers across 5 locations in Timișoara
     * Timișoara Nord: 26 lockers
     * Iulius Town: 26 lockers
     * Shopping City: 26 lockers
     * Timișoara Airport: 26 lockers
     * Piața Victoriei: 26 lockers
     */
    private List<Locker> initializeLockers() {
        List<Locker> lockers = new java.util.ArrayList<>();

        // Define locations with coordinates in Timișoara
        LocationData[] locations = {
            new LocationData("Timișoara Nord", "Strada Gării, Timișoara 300162", 45.7489, 21.2087),
            new LocationData("Iulius Town Timișoara", "Strada Aristide Demetriade 1, Timișoara 300088", 45.7662, 21.2274),
            new LocationData("Shopping City Timișoara", "Calea Șagului 100, Timișoara 300517", 45.7320, 21.2116),
            new LocationData("Timișoara Airport", "Strada Aeroport 2, Ghiroda 307210", 45.8098, 21.3198),
            new LocationData("Piața Victoriei", "Piața Victoriei, Timișoara 300006", 45.7537, 21.2257)
        };

        int lockerCounter = 1;
        int[] lockersPerLocation = {26, 26, 26, 26, 26}; // 130 total

        for (int locIdx = 0; locIdx < locations.length; locIdx++) {
            LocationData location = locations[locIdx];
            String[] floors = {"Ground Floor", "Floor 1", "Floor 2", "Floor 3"};
            String[] sections = {"Section A", "Section B", "Section C", "Section D", "Section E"};

            for (int i = 0; i < lockersPerLocation[locIdx]; i++) {
                String floor = floors[i % floors.length];
                String section = sections[i % sections.length];

                Size size = getRandomSize();
                Status status = getRandomStatus();
                Double hourlyRate = getHourlyRateBySize(size);

                Locker locker = new Locker(
                    String.format("%s-%04d", location.name.replace(" ", "").substring(0, 3), lockerCounter),
                    size,
                    status,
                    hourlyRate,
                    location.name,
                    location.address,
                    location.latitude,
                    location.longitude,
                    floor,
                    section
                );

                lockers.add(locker);
                lockerCounter++;
            }
        }

        lockerRepository.saveAll(lockers);
        return lockers;
    }

    /**
     * Initialize 30-50 bookings with realistic patterns
     */
    private void initializeBookings(List<Person> users, List<Locker> lockers) {
        List<Booking> bookings = new java.util.ArrayList<>();

        // Filter to only customer users
        List<Person> customers = users.stream()
            .filter(Person::isCustomer)
            .toList();

        // Create 40 bookings (between 30-50)
        int bookingCount = 40;
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < bookingCount; i++) {
            // Pick a random customer
            Person customer = customers.get(random.nextInt(customers.size()));

            // Pick a random locker that's either available or we override status
            Locker locker = lockers.get(random.nextInt(lockers.size()));

            // Create booking with various time patterns
            LocalDateTime startDate = getRandomStartDate(now, i);
            int durationHours = getRandomDuration();
            LocalDateTime endDate = startDate.plusHours(durationHours);

            // Ensure end date is in the future for ACTIVE bookings
            boolean shouldBeActive = random.nextBoolean() && endDate.isAfter(now);

            Booking booking = new Booking(customer, locker, startDate, endDate);

            // Set random booking status
            if (shouldBeActive) {
                booking.setStatus(BookingStatus.ACTIVE);
                locker.setStatus(Status.OCCUPIED);
            } else if (random.nextBoolean()) {
                booking.setStatus(BookingStatus.COMPLETED);
            } else {
                booking.setStatus(BookingStatus.CANCELLED);
            }

            booking.setTotalPrice(booking.calculatePrice());
            bookings.add(booking);
        }

        bookingRepository.saveAll(bookings);
    }

    /**
     * Get a random locker size with distribution:
     * 40% SMALL, 35% MEDIUM, 25% LARGE
     */
    private Size getRandomSize() {
        int rand = random.nextInt(100);
        if (rand < 40) return Size.SMALL;
        if (rand < 75) return Size.MEDIUM;
        return Size.LARGE;
    }

    /**
     * Get hourly rate based on size
     */
    private Double getHourlyRateBySize(Size size) {
        switch (size) {
            case SMALL:
                return 2.50;
            case MEDIUM:
                return 5.00;
            case LARGE:
                return 8.50;
            default:
                return 5.00;
        }
    }

    /**
     * Get a random locker status
     * 60% AVAILABLE, 25% OCCUPIED, 10% RESERVED, 4% MAINTENANCE, 1% OUT_OF_ORDER
     */
    private Status getRandomStatus() {
        int rand = random.nextInt(100);
        if (rand < 60) return Status.AVAILABLE;
        if (rand < 85) return Status.OCCUPIED;
        if (rand < 95) return Status.RESERVED;
        if (rand < 99) return Status.MAINTENANCE;
        return Status.OUT_OF_ORDER;
    }

    /**
     * Get a random start date (past, present, or future)
     */
    private LocalDateTime getRandomStartDate(LocalDateTime now, int bookingIndex) {
        // Vary the dates: some past, some present, some future
        int daysOffset = random.nextInt(60) - 20; // -20 to +40 days
        int hours = random.nextInt(24);
        int minutes = random.nextInt(60);

        return now.plusDays(daysOffset)
                  .withHour(hours)
                  .withMinute(minutes)
                  .withSecond(0)
                  .withNano(0);
    }

    /**
     * Get a random booking duration (1 to 72 hours)
     */
    private int getRandomDuration() {
        int[] commonDurations = {1, 2, 3, 4, 6, 8, 12, 24, 48, 72};
        return commonDurations[random.nextInt(commonDurations.length)];
    }
}
