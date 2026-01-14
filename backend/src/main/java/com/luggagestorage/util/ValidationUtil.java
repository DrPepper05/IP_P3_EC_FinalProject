package com.luggagestorage.util;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-zA-Z]).{6,}$"
    );

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isNotNullOrEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isPositive(Number value) {
        if (value == null) {
            return false;
        }
        return value.doubleValue() > 0;
    }

    public static boolean isNonNegative(Number value) {
        if (value == null) {
            return false;
        }
        return value.doubleValue() >= 0;
    }

    public static boolean isValidTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return false;
        }
        return startTime.isBefore(endTime);
    }

    public static boolean isFutureTime(LocalDateTime time) {
        if (time == null) {
            return false;
        }
        return time.isAfter(LocalDateTime.now());
    }

    public static boolean isNotPastTime(LocalDateTime time) {
        if (time == null) {
            return false;
        }
        return !time.isBefore(LocalDateTime.now());
    }

    public static boolean isValidLockerNumber(String lockerNumber) {
        if (lockerNumber == null || lockerNumber.trim().isEmpty()) {
            return false;
        }
        return lockerNumber.matches("^[A-Za-z0-9-]+$");
    }

    public static boolean hasMinLength(String value, int minLength) {
        if (value == null) {
            return false;
        }
        return value.length() >= minLength;
    }

    public static boolean hasMaxLength(String value, int maxLength) {
        if (value == null) {
            return true;
        }
        return value.length() <= maxLength;
    }

    public static boolean isInRange(Number value, double min, double max) {
        if (value == null) {
            return false;
        }
        double doubleValue = value.doubleValue();
        return doubleValue >= min && doubleValue <= max;
    }

    public static String sanitize(String input) {
        if (input == null) {
            return null;
        }
        return input.trim();
    }
}
