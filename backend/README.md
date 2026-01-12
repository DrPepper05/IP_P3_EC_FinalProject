# Luggage Storage System

A REST API-based web application for managing luggage storage bookings at a locker facility. Built with Spring Boot 2.7.x, featuring JWT authentication, role-based authorization, and comprehensive booking management.

## Project Information

**Author:** Bogdan Marian
**Version:** 1.0.0
**Framework:** Spring Boot 2.7.18
**Java Version:** 11+

## Features

### Core Features
- ✅ User authentication and registration with JWT tokens
- ✅ Role-based access control (Admin & Customer roles)
- ✅ Password hashing with BCrypt
- ✅ Locker management (CRUD operations)
- ✅ Booking management with automatic price calculation
- ✅ Availability checking and conflict prevention
- ✅ File I/O operations (JSON format)
- ✅ Comprehensive input validation
- ✅ Global exception handling
- ✅ RESTful API design

### Optional Features Implemented (Full 4 Points)
1. ✅ **Configuration Support (1 point):** Application profiles (dev/prod) and command-line arguments
2. ✅ **Language Exception Handling (1 point):** Handles FileNotFoundException, IOException, IllegalArgumentException, NullPointerException
3. ✅ **Custom Exceptions (1 point):** LockerNotAvailableException, InvalidBookingTimeException, ResourceNotFoundException
4. ✅ **Input Validation (1 point):** Comprehensive validation for all user inputs using Bean Validation and custom ValidationUtil

### Technical Highlights
- **4+ Entity Classes:** Person, Locker, Booking, + DTOs
- **3+ Enums:** Size, Status, BookingStatus, Role
- **2+ Collections:** List<Booking> in Person and Locker entities
- **JWT Authentication:** Secure token-based authentication
- **File Storage:** JSON-based persistence for data backup
- **Layered Architecture:** Controller → Service → Repository
- **Spring Security:** Complete security configuration with role-based authorization

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- MySQL 8.0+ (for database)
- Any REST API client (Postman, cURL, etc.)

## Setup Instructions

### 1. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE luggage_storage_db;
```

### 2. Clone and Build

```bash
# Navigate to project directory
cd luggage-storage-system

# Build the project
mvn clean install

# Run the application (development mode)
mvn spring-boot:run

# Or run with production profile
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=prod
```

### 3. Configuration Options

#### Using Application Profiles

**Development Mode (default):**
```bash
java -jar target/luggage-storage-system-1.0.0.jar
```

**Production Mode:**
```bash
java -jar target/luggage-storage-system-1.0.0.jar --spring.profiles.active=prod
```

#### Using Command-Line Arguments

```bash
# Custom port
java -jar target/luggage-storage-system-1.0.0.jar --server.port=9090

# Custom database
java -jar target/luggage-storage-system-1.0.0.jar --spring.datasource.url=jdbc:mysql://localhost:3306/mydb --spring.datasource.username=user --spring.datasource.password=pass

# Disable file storage
java -jar target/luggage-storage-system-1.0.0.jar --file.storage.enabled=false

# Custom file storage path
java -jar target/luggage-storage-system-1.0.0.jar --file.storage.path=/custom/path
```

### 4. Default Configuration

The application runs on `http://localhost:8080` with the following defaults:
- Profile: dev
- Database: `luggage_storage_db` on localhost:3306
- Username: root
- Password: root
- File Storage: enabled (`./data` directory)

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication Endpoints

#### 1. Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CUSTOMER"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "userId": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CUSTOMER"
}
```

#### 2. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Response:** Same as registration

#### 3. Health Check
```http
GET /api/auth/health
```

### Locker Endpoints

#### 1. Get All Available Lockers (Public)
```http
GET /api/lockers/available
```

#### 2. Get Locker by ID (Public)
```http
GET /api/lockers/{id}
```

#### 3. Get Lockers by Size (Public)
```http
GET /api/lockers/available/size/SMALL
```

Sizes: `SMALL`, `MEDIUM`, `LARGE`

#### 4. Create Locker (Admin Only)
```http
POST /api/lockers
Authorization: Bearer {token}
Content-Type: application/json

{
  "lockerNumber": "L001",
  "size": "MEDIUM",
  "status": "AVAILABLE",
  "hourlyRate": 5.0
}
```

#### 5. Update Locker (Admin Only)
```http
PUT /api/lockers/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "lockerNumber": "L001",
  "size": "MEDIUM",
  "status": "OCCUPIED",
  "hourlyRate": 6.0
}
```

#### 6. Delete Locker (Admin Only)
```http
DELETE /api/lockers/{id}
Authorization: Bearer {token}
```

### Booking Endpoints

#### 1. Create Booking (Customer/Admin)
```http
POST /api/bookings
Authorization: Bearer {token}
Content-Type: application/json

{
  "lockerId": 1,
  "startDatetime": "2025-12-01T10:00:00",
  "endDatetime": "2025-12-01T14:00:00"
}
```

**Response:**
```json
{
  "id": 1,
  "customerId": 1,
  "customerName": "John Doe",
  "lockerId": 1,
  "lockerNumber": "L001",
  "lockerSize": "MEDIUM",
  "startDatetime": "2025-12-01T10:00:00",
  "endDatetime": "2025-12-01T14:00:00",
  "status": "ACTIVE",
  "totalPrice": 20.0,
  "durationInHours": 4
}
```

#### 2. Get My Bookings (Customer)
```http
GET /api/bookings/my-bookings
Authorization: Bearer {token}
```

#### 3. Get All Bookings (Admin Only)
```http
GET /api/bookings
Authorization: Bearer {token}
```

#### 4. Cancel Booking (Customer/Admin)
```http
PUT /api/bookings/{id}/cancel
Authorization: Bearer {token}
```

#### 5. Complete Booking (Admin Only)
```http
PUT /api/bookings/{id}/complete
Authorization: Bearer {token}
```

### Person Management Endpoints (Admin Only)

#### 1. Get All Users
```http
GET /api/persons
Authorization: Bearer {token}
```

#### 2. Get All Customers
```http
GET /api/persons/customers
Authorization: Bearer {token}
```

#### 3. Get User by ID
```http
GET /api/persons/{id}
Authorization: Bearer {token}
```

## Project Structure

```
luggage-storage-system/
├── src/main/java/com/luggagestorage/
│   ├── config/
│   │   ├── SecurityConfig.java           # Spring Security configuration
│   │   ├── AppConfig.java                # Application configuration
│   │   ├── JwtTokenProvider.java         # JWT token utilities
│   │   └── JwtAuthenticationFilter.java  # JWT filter
│   ├── model/
│   │   ├── Person.java                   # User entity (4+ classes requirement)
│   │   ├── Locker.java                   # Locker entity
│   │   ├── Booking.java                  # Booking entity
│   │   ├── enums/
│   │   │   ├── Size.java                 # Locker size enum (2+ enums requirement)
│   │   │   ├── Status.java               # Locker status enum
│   │   │   ├── BookingStatus.java        # Booking status enum
│   │   │   └── Role.java                 # User role enum
│   │   └── dto/                          # Data Transfer Objects
│   ├── repository/                       # Spring Data JPA repositories
│   ├── service/                          # Business logic layer
│   │   ├── PersonService.java
│   │   ├── LockerService.java
│   │   ├── BookingService.java
│   │   ├── AuthService.java
│   │   ├── FileStorageService.java       # File I/O service (1 point requirement)
│   │   └── CustomUserDetailsService.java
│   ├── controller/                       # REST controllers
│   │   ├── AuthController.java
│   │   ├── PersonController.java
│   │   ├── LockerController.java
│   │   └── BookingController.java
│   ├── exception/                        # Custom exceptions
│   │   ├── LockerNotAvailableException.java    # Custom exception (1 point)
│   │   ├── InvalidBookingTimeException.java    # Custom exception (1 point)
│   │   ├── ResourceNotFoundException.java      # Custom exception
│   │   └── GlobalExceptionHandler.java         # Exception handler (1 point)
│   ├── util/
│   │   └── ValidationUtil.java           # Validation utilities (1 point)
│   └── LuggageStorageApplication.java    # Main application class
├── src/main/resources/
│   ├── application.properties            # Default configuration
│   ├── application-dev.properties        # Development profile (1 point)
│   └── application-prod.properties       # Production profile (1 point)
├── docs/
│   └── diagrams/                         # UML diagrams
├── pom.xml                               # Maven configuration
└── README.md                             # This file
```

## Business Logic

### Price Calculation
- Booking price = Duration (hours) × Hourly Rate
- Minimum duration: 1 hour
- Price is automatically calculated when booking is created

### Locker Availability
- Lockers have two statuses: AVAILABLE and OCCUPIED
- System prevents double-booking by checking for overlapping reservations
- Locker status automatically updates when booking is created/completed/cancelled

### Booking Lifecycle
1. **ACTIVE:** Booking is currently active
2. **COMPLETED:** Booking has been completed (locker returned to customer)
3. **CANCELLED:** Booking was cancelled

### Authorization Rules
- **Public:** View available lockers
- **Customer:** Create bookings, view own bookings, cancel own bookings
- **Admin:** Full access to all endpoints, manage lockers, view all bookings

## Exception Handling

The application handles the following exceptions:

### Language Exceptions (1 Point Requirement)
1. **FileNotFoundException:** When file storage operations fail
2. **IOException:** When I/O operations fail
3. **IllegalArgumentException:** When invalid arguments are provided
4. **NullPointerException:** When null references are encountered

### Custom Exceptions (1 Point Requirement)
1. **LockerNotAvailableException:** When a locker is unavailable or double-booked
2. **InvalidBookingTimeException:** When booking times are invalid
3. **ResourceNotFoundException:** When a requested resource doesn't exist

All exceptions return structured error responses with timestamp, status, message, and details.

## File I/O Operations (1 Point Requirement)

The application stores data in JSON format:
- **Location:** `./data` directory (configurable)
- **Files:** persons.json, lockers.json, bookings.json
- **Format:** JSON with Jackson library
- **Auto-save:** Data is saved after each modification

## Input Validation (1 Point Requirement)

### Bean Validation (DTOs)
- Email format validation
- Required field validation
- Minimum/maximum length validation
- Positive number validation

### Custom Validation (ValidationUtil)
- Password strength validation
- Time range validation
- Locker number format validation
- Custom business rule validation

## Security Features

- **Password Hashing:** BCrypt with salt
- **JWT Tokens:** Secure token-based authentication
- **Token Expiration:** 24 hours (configurable)
- **Role-Based Authorization:** @PreAuthorize annotations
- **CORS:** Configurable cross-origin support

## Testing

### Manual Testing with cURL

**Register:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123","firstName":"Test","lastName":"User","role":"CUSTOMER"}'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'
```

**Get Available Lockers:**
```bash
curl http://localhost:8080/api/lockers/available
```

**Create Booking:**
```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{"lockerId":1,"startDatetime":"2025-12-01T10:00:00","endDatetime":"2025-12-01T14:00:00"}'
```

## Requirements Checklist

### Mandatory Requirements (6 Points Minimum)
- ✅ **Presentation (2p):** Ready for presentation
- ✅ **UML Diagram (3p):** Complete UML class diagram included
- ✅ **File I/O (1p):** JSON file storage implemented

### Optional Requirements (4 Points Maximum)
- ✅ **Configuration (1p):** Profiles and CLI arguments supported
- ✅ **Exception Handling (1p):** 4+ language exceptions handled
- ✅ **Custom Exceptions (1p):** 3 custom exceptions created
- ✅ **Input Validation (1p):** Comprehensive validation implemented

**Total Points:** 10/10

## Troubleshooting

### Database Connection Issues
```bash
# Check MySQL is running
mysql -u root -p

# Create database manually
CREATE DATABASE luggage_storage_db;
```

### Port Already in Use
```bash
# Use a different port
java -jar target/luggage-storage-system-1.0.0.jar --server.port=9090
```

### File Storage Issues
```bash
# Check data directory permissions
ls -la ./data

# Disable file storage if needed
java -jar target/luggage-storage-system-1.0.0.jar --file.storage.enabled=false
```

## License

This project is developed as a midterm project for academic purposes.

## Contact

For questions or issues, please contact Bogdan Marian.
