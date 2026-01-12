package com.luggagestorage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Main application class for the Luggage Storage System.
 * Supports configuration via application.properties profiles and command-line arguments.
 * Part of the optional requirement: "Configuration files and/or program arguments" (1 point).
 *
 * Usage examples:
 * - Default (dev profile): java -jar luggage-storage-system.jar
 * - Production profile: java -jar luggage-storage-system.jar --spring.profiles.active=prod
 * - Custom port: java -jar luggage-storage-system.jar --server.port=9090
 * - Custom database: java -jar luggage-storage-system.jar --spring.datasource.url=jdbc:mysql://localhost:3306/mydb
 */
@SpringBootApplication
@EnableScheduling
public class LuggageStorageApplication {

    private static final Logger logger = LoggerFactory.getLogger(LuggageStorageApplication.class);

    public static void main(String[] args) {
        // Print startup banner
        printBanner();

        // Start Spring Boot application
        ConfigurableApplicationContext context = SpringApplication.run(LuggageStorageApplication.class, args);

        // Log application startup information
        logApplicationStartup(context);
    }

    /**
     * Print application banner.
     */
    private static void printBanner() {
        System.out.println("\n=======================================================");
        System.out.println("   LUGGAGE STORAGE SYSTEM");
        System.out.println("   Version: 1.0.0");
        System.out.println("   REST API with JWT Authentication");
        System.out.println("=======================================================\n");
    }

    /**
     * Log application startup information including:
     * - Active profiles
     * - Server port
     * - Local and external URLs
     * - Database URL
     */
    private static void logApplicationStartup(ConfigurableApplicationContext context) {
        Environment env = context.getEnvironment();

        String protocol = "http";
        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        String hostAddress = "localhost";

        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("The host name could not be determined, using `localhost` as fallback");
        }

        String[] activeProfiles = env.getActiveProfiles();
        String profiles = activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "default";

        logger.info("\n----------------------------------------------------------\n" +
                        "Application '{}' is running!\n" +
                        "Profile(s): {}\n" +
                        "Local:      {}://localhost:{}{}\n" +
                        "External:   {}://{}:{}{}\n" +
                        "Database:   {}\n" +
                        "API Docs:   See README.md for endpoint documentation\n" +
                        "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                profiles,
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getProperty("spring.datasource.url", "Not configured"));

        // Log configuration options
        logger.info("\n=== Configuration Options ===");
        logger.info("File Storage Enabled: {}", env.getProperty("file.storage.enabled", "true"));
        logger.info("File Storage Path: {}", env.getProperty("file.storage.path", "./data"));
        logger.info("JWT Expiration: {} ms", env.getProperty("jwt.expiration", "86400000"));

        // Log important endpoints
        logger.info("\n=== Important Endpoints ===");
        logger.info("Health Check: {}://localhost:{}/api/auth/health", protocol, serverPort);
        logger.info("Register: POST {}://localhost:{}/api/auth/register", protocol, serverPort);
        logger.info("Login: POST {}://localhost:{}/api/auth/login", protocol, serverPort);
        logger.info("View Lockers: GET {}://localhost:{}/api/lockers/available", protocol, serverPort);

        // Log command-line argument options
        logger.info("\n=== Command-Line Argument Examples ===");
        logger.info("Change profile: --spring.profiles.active=prod");
        logger.info("Change port: --server.port=9090");
        logger.info("Change database: --spring.datasource.url=jdbc:mysql://localhost:3306/mydb");
        logger.info("Disable file storage: --file.storage.enabled=false");
        logger.info("Change file storage path: --file.storage.path=/custom/path");

        logger.info("\n----------------------------------------------------------\n");
    }
}
