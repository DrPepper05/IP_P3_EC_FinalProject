package com.luggagestorage.service;

import com.luggagestorage.exception.ResourceNotFoundException;
import com.luggagestorage.model.Person;
import com.luggagestorage.model.enums.Role;
import com.luggagestorage.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Person entity.
 * Provides CRUD operations and business logic for user management.
 */
@Service
@Transactional
public class PersonService {

    private static final Logger logger = LoggerFactory.getLogger(PersonService.class);
    private static final String PERSONS_FILE = "persons.json";

    private final PersonRepository personRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public PersonService(PersonRepository personRepository, FileStorageService fileStorageService) {
        this.personRepository = personRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Create a new person.
     *
     * @param person The person to create
     * @return The created person
     */
    public Person createPerson(Person person) {
        logger.info("Creating new person with email: {}", person.getEmail());

        // Check if email already exists
        if (personRepository.existsByEmail(person.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + person.getEmail());
        }

        Person savedPerson = personRepository.save(person);
        saveToFile();
        logger.info("Person created successfully with ID: {}", savedPerson.getId());
        return savedPerson;
    }

    /**
     * Get a person by ID.
     *
     * @param id The person ID
     * @return The person
     * @throws ResourceNotFoundException if person not found
     */
    public Person getPersonById(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person", "id", id));
    }

    /**
     * Get a person by email.
     *
     * @param email The email address
     * @return Optional containing the person if found
     */
    public Optional<Person> getPersonByEmail(String email) {
        return personRepository.findByEmail(email);
    }

    /**
     * Get all persons.
     *
     * @return List of all persons
     */
    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    /**
     * Get all persons with a specific role.
     *
     * @param role The role to filter by
     * @return List of persons with the specified role
     */
    public List<Person> getPersonsByRole(Role role) {
        return personRepository.findByRole(role);
    }

    /**
     * Get all customers.
     *
     * @return List of all customers
     */
    public List<Person> getAllCustomers() {
        return personRepository.findByRole(Role.CUSTOMER);
    }

    /**
     * Get all admins.
     *
     * @return List of all admins
     */
    public List<Person> getAllAdmins() {
        return personRepository.findByRole(Role.ADMIN);
    }

    /**
     * Update a person.
     *
     * @param id     The person ID
     * @param person The updated person data
     * @return The updated person
     * @throws ResourceNotFoundException if person not found
     */
    public Person updatePerson(Long id, Person person) {
        logger.info("Updating person with ID: {}", id);

        Person existingPerson = getPersonById(id);

        // Update fields
        if (person.getEmail() != null && !person.getEmail().equals(existingPerson.getEmail())) {
            // Check if new email already exists
            if (personRepository.existsByEmail(person.getEmail())) {
                throw new IllegalArgumentException("Email already exists: " + person.getEmail());
            }
            existingPerson.setEmail(person.getEmail());
        }

        if (person.getFirstName() != null) {
            existingPerson.setFirstName(person.getFirstName());
        }

        if (person.getLastName() != null) {
            existingPerson.setLastName(person.getLastName());
        }

        if (person.getRole() != null) {
            existingPerson.setRole(person.getRole());
        }

        Person updatedPerson = personRepository.save(existingPerson);
        saveToFile();
        logger.info("Person updated successfully with ID: {}", updatedPerson.getId());
        return updatedPerson;
    }

    /**
     * Update a person's role.
     *
     * @param id The person ID
     * @param role The new role
     * @return The updated person
     * @throws ResourceNotFoundException if person not found
     */
    public Person updatePersonRole(Long id, Role role) {
        logger.info("Updating role for person ID: {} to {}", id, role);

        Person person = getPersonById(id);
        person.setRole(role);
        Person updatedPerson = personRepository.save(person);
        saveToFile();
        logger.info("Person role updated successfully for ID: {}", id);
        return updatedPerson;
    }

    /**
     * Delete a person by ID.
     *
     * @param id The person ID
     * @throws ResourceNotFoundException if person not found
     */
    public void deletePerson(Long id) {
        logger.info("Deleting person with ID: {}", id);

        Person person = getPersonById(id);
        personRepository.delete(person);
        saveToFile();
        logger.info("Person deleted successfully with ID: {}", id);
    }

    /**
     * Check if a person exists by email.
     *
     * @param email The email address
     * @return true if exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return personRepository.existsByEmail(email);
    }

    /**
     * Search persons by first name.
     *
     * @param firstName The first name to search for
     * @return List of matching persons
     */
    public List<Person> searchByFirstName(String firstName) {
        return personRepository.findByFirstNameContainingIgnoreCase(firstName);
    }

    /**
     * Search persons by last name.
     *
     * @param lastName The last name to search for
     * @return List of matching persons
     */
    public List<Person> searchByLastName(String lastName) {
        return personRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    /**
     * Save all persons to JSON file.
     * Part of the file I/O requirement.
     */
    private void saveToFile() {
        try {
            List<Person> persons = personRepository.findAll();
            fileStorageService.saveToFile(persons, PERSONS_FILE);
        } catch (IOException e) {
            logger.error("Failed to save persons to file: {}", e.getMessage());
            // Don't throw exception - file storage is supplementary
        }
    }

    /**
     * Load persons from JSON file.
     * Part of the file I/O requirement.
     */
    public void loadFromFile() {
        try {
            if (fileStorageService.fileExists(PERSONS_FILE)) {
                List<Person> persons = fileStorageService.loadFromFile(PERSONS_FILE, Person.class);
                logger.info("Loaded {} persons from file", persons.size());
                // Note: In a real application, you'd need to handle ID conflicts
                // For this demo, we assume the database is empty when loading from file
            }
        } catch (IOException e) {
            logger.error("Failed to load persons from file: {}", e.getMessage());
        }
    }
}
