# Luggage Storage System

A full-stack web application for managing luggage storage lockers in Timișoara. The system allows users to find, book, and manage storage lockers, while providing administrators with comprehensive management tools.

## Features

- **User Features:**
  - Browse available storage locations
  - Interactive map with real-time availability
  - Book lockers with flexible duration
  - Manage personal bookings
  - User authentication and registration

- **Admin Features:**
  - Manage lockers (create, update, delete)
  - Monitor and manage bookings
  - User management with role assignment
  - Real-time dashboard with statistics

## Tech Stack

- **Backend:** Spring Boot 3.x, Java 17, MySQL
- **Frontend:** React 18, React Router, Leaflet Maps
- **Authentication:** JWT-based authentication
- **Styling:** CSS3 with modern design principles

## Prerequisites

- Java 17 or higher
- Node.js 16 or higher
- npm or yarn
- MySQL 8.0 or higher
- Maven 3.6 or higher

## Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/DrPepper05/IP_P3_EC_FinalProject.git
cd IP_P3_EC_FinalProject
```

### 2. Database Setup

1. Create a MySQL database:
```sql
CREATE DATABASE luggage_storage_db;
```

2. Update database credentials in `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/luggage_storage_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Backend Setup

1. Navigate to the backend directory:
```bash
cd backend
```

2. Install dependencies and build:
```bash
mvn clean install
```

3. Run the Spring Boot application:
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### 4. Frontend Setup

1. Open a new terminal and navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm start
```

The frontend will start on `http://localhost:3000`

## Usage

### Default Admin Account

On first run, the application creates a default admin account:
- **Email:** admin@luggagestorage.com
- **Password:** Admin123!

### User Registration

New users can register through the registration page with:
- Valid email address
- Password (minimum 6 characters)
- First and last name

### Application Flow

1. **Users** can:
   - Search for storage locations
   - View available lockers on the map
   - Book lockers for desired duration
   - Manage their bookings

2. **Admins** can:
   - Access the admin dashboard at `/admin`
   - Manage all lockers (add, edit, delete)
   - View and manage all bookings
   - Promote users to admin or demote admins to customers

## API Endpoints

The backend provides RESTful APIs:

- **Auth:** `/api/auth/login`, `/api/auth/register`
- **Lockers:** `/api/lockers` (CRUD operations)
- **Bookings:** `/api/bookings` (CRUD operations)
- **Users:** `/api/persons` (Admin only)

## Project Structure

```
luggage-storage-system/
├── backend/                 # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/       # Java source files
│   │   │   └── resources/  # Application properties
│   └── pom.xml             # Maven configuration
├── frontend/                # React frontend
│   ├── src/
│   │   ├── components/     # React components
│   │   ├── services/       # API services
│   │   └── App.js         # Main app component
│   └── package.json        # NPM configuration
└── README.md               # This file
```

## Troubleshooting

### Backend Issues

1. **Database connection error:**
   - Ensure MySQL is running
   - Verify database credentials in application.properties
   - Check if database exists

2. **Port 8080 already in use:**
   - Change port in application.properties: `server.port=8081`

### Frontend Issues

1. **Port 3000 already in use:**
   - Change port: `PORT=3001 npm start`

2. **API connection error:**
   - Ensure backend is running on correct port
   - Check CORS settings if accessing from different domain

## Development Notes

- The application uses JWT tokens for authentication (24-hour validity)
- File storage is implemented for data persistence alongside database
- Map functionality requires internet connection for tile loading

## License

This project is part of an academic assignment.

## Authors

IP_P3_EC Team - Final Project for Software Engineering Course