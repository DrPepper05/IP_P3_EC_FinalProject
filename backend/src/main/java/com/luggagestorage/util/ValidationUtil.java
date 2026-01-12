package com.luggagestorage.util;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * Utility class for custom validation logic.
 * Part of the optional requirement: "Validate all input data from users" (1 point).
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$"
    );

    /**
     * Validate email format.
     *
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate password strength.
     * Password must be at least 6 characters and contain both letters and numbers.
     *
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate that a string is not null or empty.
     *
     * @param value The string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isNotNullOrEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validate that a number is positive.
     *
     * @param value The number to validate
     * @return true if positive, false otherwise
     */
    public static boolean isPositive(Number value) {
        if (value == null) {
            return false;
        }
        return value.doubleValue() > 0;
    }

    /**
     * Validate that a number is non-negative.
     *
     * @param value The number to validate
     * @return true if non-negative, false otherwise
     */
    public static boolean isNonNegative(Number value) {
        if (value == null) {
            return false;
        }
        return value.doubleValue() >= 0;
    }

    /**
     * Validate booking time range.
     * Start time must be before end time.
     *
     * @param startTime The start time
     * @param endTime   The end time
     * @return true if valid, false otherwise
     */
    public static boolean isValidTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        return startTime.isBefore(endTime);
    }

    /**
     * Validate that a time is in the future.
     *
     * @param time The time to validate
     * @return true if in the future, false otherwise
     */
    public static boolean isFutureTime(LocalDateTime time) {
        if (time == null) {
            return false;
        }
        return time.isAfter(LocalDateTime.now());
    }

    /**
     * Validate that a time is not in the past.
     *
     * @param time The time to validate
     * @return true if not in the past, false otherwise
     */
    public static boolean isNotPastTime(LocalDateTime time) {
        if (time == null) {
            return false;
        }
        return !time.isBefore(LocalDateTime.now());
    }

    /**
     * Validate locker number format.
     * Locker number should be alphanumeric.
     *
     * @param lockerNumber The locker number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidLockerNumber(String lockerNumber) {
        if (lockerNumber == null || lockerNumber.trim().isEmpty()) {
            return false;
        }
        return lockerNumber.matches("^[A-Za-z0-9-]+$");
    }

    /**
     * Validate that a string has minimum length.
     *
     * @param value     The string to validate
     * @param minLength The minimum length
     * @return true if valid, false otherwise
     */
    public static boolean hasMinLength(String value, int minLength) {
        if (value == null) {
            return false;
        }
        return value.length() >= minLength;
    }

    /**
     * Validate that a string has maximum length.
     *
     * @param value     The string to validate
     * @param maxLength The maximum length
     * @return true if valid, false otherwise
     */
    public static boolean hasMaxLength(String value, int maxLength) {
        if (value == null) {
            return true; // null is considered valid for max length
        }
        return value.length() <= maxLength;
    }

    /**
     * Validate that a number is within a range.
     *
     * @param value The number to validate
     * @param min   The minimum value (inclusive)
     * @param max   The maximum value (inclusive)
     * @return true if within range, false otherwise
     */
    public static boolean isInRange(Number value, double min, double max) {
        if (value == null) {
            return false;
        }
        double doubleValue = value.doubleValue();
        return doubleValue >= min && doubleValue <= max;
    }

    /**
     * Sanitize input by trimming whitespace.
     *
     * @param input The input string
     * @return The sanitized string
     */
    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim();
    }
}
