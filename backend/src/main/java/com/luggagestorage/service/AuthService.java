package com.luggagestorage.service;

import com.luggagestorage.config.JwtTokenProvider;
import com.luggagestorage.model.Person;
import com.luggagestorage.model.dto.AuthResponse;
import com.luggagestorage.model.dto.LoginRequest;
import com.luggagestorage.model.dto.RegisterRequest;
import com.luggagestorage.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for authentication operations (registration and login).
 * Implements password hashing and JWT token generation.
 */
@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthService(PersonRepository personRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider jwtTokenProvider) {
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Register a new user.
     * Implements password hashing (security requirement).
     *
     * @param registerRequest The registration request
     * @return Authentication response with JWT token
     */
    public AuthResponse register(RegisterRequest registerRequest) {
        logger.info("Registering new user with email: {}", registerRequest.getEmail());

        // Check if email already exists
        if (personRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + registerRequest.getEmail());
        }

        // Validate password strength (optional - part of input validation)
        if (registerRequest.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        // Create new person with hashed password
        Person person = new Person();
        person.setEmail(registerRequest.getEmail());
        person.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword())); // Hash password
        person.setFirstName(registerRequest.getFirstName());
        person.setLastName(registerRequest.getLastName());
        person.setRole(registerRequest.getRole());

        Person savedPerson = personRepository.save(person);
        logger.info("User registered successfully with ID: {}", savedPerson.getId());

        // Generate JWT token
        String roles = savedPerson.getRole().getAuthority();
        String token = jwtTokenProvider.generateTokenFromUsername(savedPerson.getEmail(), roles);

        // Create response
        return new AuthResponse(
                token,
                savedPerson.getId(),
                savedPerson.getEmail(),
                savedPerson.getFirstName(),
                savedPerson.getLastName(),
                savedPerson.getRole()
        );
    }

    /**
     * Authenticate user and generate JWT token.
     * Part of the login() method requirement from Person entity.
     *
     * @param loginRequest The login request
     * @return Authentication response with JWT token
     * @throws BadCredentialsException if credentials are invalid
     */
    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("User attempting to login with email: {}", loginRequest.getEmail());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);

            // Get user details
            Person person = personRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            logger.info("User logged in successfully: {}", person.getEmail());

            // Create response
            return new AuthResponse(
                    token,
                    person.getId(),
                    person.getEmail(),
                    person.getFirstName(),
                    person.getLastName(),
                    person.getRole()
            );

        } catch (BadCredentialsException ex) {
            logger.error("Login failed for email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    /**
     * Get current authenticated user.
     *
     * @return The current user
     */
    public Person getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        return personRepository.findByEmail(email).orElse(null);
    }

    /**
     * Check if current user is authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser");
    }

    /**
     * Check if current user has admin role.
     *
     * @return true if admin, false otherwise
     */
    public boolean isCurrentUserAdmin() {
        Person currentUser = getCurrentUser();
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Logout current user.
     * Clears the security context.
     */
    public void logout() {
        SecurityContextHolder.clearContext();
        logger.info("User logged out successfully");
    }
}
