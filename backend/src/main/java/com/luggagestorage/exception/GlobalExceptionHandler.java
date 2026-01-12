package com.luggagestorage.exception;

import com.luggagestorage.model.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Global exception handler for the application.
 * Handles both custom exceptions and language exceptions.
 * Part of the optional requirement: "Identify and handle at least 3 language exceptions" (1 point).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle custom exception: ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle custom exception: LockerNotAvailableException
     */
    @ExceptionHandler(LockerNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleLockerNotAvailableException(
            LockerNotAvailableException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        if (ex.getLockerId() != null) {
            error.addDetail("Locker ID: " + ex.getLockerId());
        }
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handle custom exception: InvalidBookingTimeException
     */
    @ExceptionHandler(InvalidBookingTimeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBookingTimeException(
            InvalidBookingTimeException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        if (ex.getStartTime() != null && ex.getEndTime() != null) {
            error.addDetail("Start time: " + ex.getStartTime());
            error.addDetail("End time: " + ex.getEndTime());
        }
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle language exception #1: FileNotFoundException
     * Part of the optional requirement (1 point).
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleFileNotFoundException(
            FileNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "File Not Found",
                "The requested file could not be found: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        error.addDetail("This error occurs when trying to access a file that doesn't exist");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle language exception #2: IOException
     * Part of the optional requirement (1 point).
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(
            IOException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "I/O Error",
                "An I/O error occurred: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        error.addDetail("This error occurs during file read/write operations");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle language exception #3: IllegalArgumentException
     * Part of the optional requirement (1 point).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid Argument",
                "Invalid argument provided: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        error.addDetail("This error occurs when an illegal or inappropriate argument is passed to a method");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle language exception #4: NullPointerException (bonus)
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(
            NullPointerException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Null Pointer Error",
                "A null pointer error occurred: " + (ex.getMessage() != null ? ex.getMessage() : "Object reference was null"),
                request.getDescription(false).replace("uri=", "")
        );
        error.addDetail("This error occurs when trying to access a null object reference");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle validation errors from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Input validation failed",
                request.getDescription(false).replace("uri=", "")
        );

        // Add all validation errors to details
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            error.addDetail(fieldError.getField() + ": " + fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid credentials: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle generic authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Authentication failed: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred: " + ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        error.addDetail("Exception type: " + ex.getClass().getSimpleName());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
