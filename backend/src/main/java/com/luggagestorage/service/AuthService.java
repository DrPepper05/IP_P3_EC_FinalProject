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

    public AuthResponse register(RegisterRequest registerRequest) {
        logger.info("Registering new user with email: {}", registerRequest.getEmail());

        if (personRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + registerRequest.getEmail());
        }

        if (registerRequest.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }

        Person person = new Person();
        person.setEmail(registerRequest.getEmail());
        person.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        person.setFirstName(registerRequest.getFirstName());
        person.setLastName(registerRequest.getLastName());
        person.setRole(registerRequest.getRole());

        Person savedPerson = personRepository.save(person);
        logger.info("User registered successfully with ID: {}", savedPerson.getId());

        String roles = savedPerson.getRole().getAuthority();
        String token = jwtTokenProvider.generateTokenFromUsername(savedPerson.getEmail(), roles);

        return new AuthResponse(
                token,
                savedPerson.getId(),
                savedPerson.getEmail(),
                savedPerson.getFirstName(),
                savedPerson.getLastName(),
                savedPerson.getRole()
        );
    }

    public AuthResponse login(LoginRequest loginRequest) {
        logger.info("User attempting to login with email: {}", loginRequest.getEmail());

        try {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtTokenProvider.generateToken(authentication);

            Person person = personRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            logger.info("User logged in successfully: {}", person.getEmail());

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

    public Person getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String email = authentication.getName();
        return personRepository.findByEmail(email).orElse(null);
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser");
    }

    public boolean isCurrentUserAdmin() {
        Person currentUser = getCurrentUser();
        return currentUser != null && currentUser.isAdmin();
    }

    public void logout() {
        SecurityContextHolder.clearContext();
        logger.info("User logged out successfully");
    }
}
