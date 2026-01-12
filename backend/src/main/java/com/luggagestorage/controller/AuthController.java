package com.luggagestorage.controller;

import com.luggagestorage.model.dto.AuthResponse;
import com.luggagestorage.model.dto.LoginRequest;
import com.luggagestorage.model.dto.RegisterRequest;
import com.luggagestorage.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST Controller for authentication endpoints.
 * Provides register and login functionality with JWT token generation.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Register a new user.
     * POST /api/auth/register
     *
     * @param registerRequest The registration request with validation
     * @return Authentication response with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.register(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Login user and generate JWT token.
     * POST /api/auth/login
     *
     * @param loginRequest The login request with validation
     * @return Authentication response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout current user.
     * POST /api/auth/logout
     *
     * @return Success message
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        authService.logout();
        return ResponseEntity.ok("Logged out successfully");
    }

    /**
     * Health check endpoint.
     * GET /api/auth/health
     *
     * @return Status message
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
