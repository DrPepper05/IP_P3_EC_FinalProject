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

    public Person createPerson(Person person) {
        logger.info("Creating new person with email: {}", person.getEmail());

        if (personRepository.existsByEmail(person.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + person.getEmail());
        }

        Person savedPerson = personRepository.save(person);
        saveToFile();
        logger.info("Person created successfully with ID: {}", savedPerson.getId());
        return savedPerson;
    }

    public Person getPersonById(Long id) {
        return personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Person", "id", id));
    }

    public Optional<Person> getPersonByEmail(String email) {
        return personRepository.findByEmail(email);
    }

    public List<Person> getAllPersons() {
        return personRepository.findAll();
    }

    public List<Person> getPersonsByRole(Role role) {
        return personRepository.findByRole(role);
    }

    public List<Person> getAllCustomers() {
        return personRepository.findByRole(Role.CUSTOMER);
    }

    public List<Person> getAllAdmins() {
        return personRepository.findByRole(Role.ADMIN);
    }

    public Person updatePerson(Long id, Person person) {
        logger.info("Updating person with ID: {}", id);

        Person existingPerson = getPersonById(id);

        if (person.getEmail() != null && !person.getEmail().equals(existingPerson.getEmail())) {

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

    public Person updatePersonRole(Long id, Role role) {
        logger.info("Updating role for person ID: {} to {}", id, role);

        Person person = getPersonById(id);
        person.setRole(role);
        Person updatedPerson = personRepository.save(person);
        saveToFile();
        logger.info("Person role updated successfully for ID: {}", id);
        return updatedPerson;
    }

    public void deletePerson(Long id) {
        logger.info("Deleting person with ID: {}", id);

        Person person = getPersonById(id);
        personRepository.delete(person);
        saveToFile();
        logger.info("Person deleted successfully with ID: {}", id);
    }

    public boolean existsByEmail(String email) {
        return personRepository.existsByEmail(email);
    }

    public List<Person> searchByFirstName(String firstName) {
        return personRepository.findByFirstNameContainingIgnoreCase(firstName);
    }

    public List<Person> searchByLastName(String lastName) {
        return personRepository.findByLastNameContainingIgnoreCase(lastName);
    }

    private void saveToFile() {
        try {
            List<Person> persons = personRepository.findAll();
            fileStorageService.saveToFile(persons, PERSONS_FILE);
        } catch (IOException e) {
            logger.error("Failed to save persons to file: {}", e.getMessage());

        }
    }

    public void loadFromFile() {
        try {
            if (fileStorageService.fileExists(PERSONS_FILE)) {
                List<Person> persons = fileStorageService.loadFromFile(PERSONS_FILE, Person.class);
                logger.info("Loaded {} persons from file", persons.size());

            }
        } catch (IOException e) {
            logger.error("Failed to load persons from file: {}", e.getMessage());
        }
    }
}
