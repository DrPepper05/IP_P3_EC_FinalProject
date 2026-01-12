package com.luggagestorage.controller;

import com.luggagestorage.model.Person;
import com.luggagestorage.model.enums.Role;
import com.luggagestorage.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST Controller for Person (User) management endpoints.
 * All endpoints require ADMIN role (role-based authorization).
 */
@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "*")
public class PersonController {

    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Get all persons.
     * GET /api/persons
     * Admin only
     *
     * @return List of all persons
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> getAllPersons() {
        List<Person> persons = personService.getAllPersons();
        return ResponseEntity.ok(persons);
    }

    /**
     * Get a person by ID.
     * GET /api/persons/{id}
     * Admin only
     *
     * @param id The person ID
     * @return The person
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
        Person person = personService.getPersonById(id);
        return ResponseEntity.ok(person);
    }

    /**
     * Get all customers.
     * GET /api/persons/customers
     * Admin only
     *
     * @return List of all customers
     */
    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> getAllCustomers() {
        List<Person> customers = personService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * Get all admins.
     * GET /api/persons/admins
     * Admin only
     *
     * @return List of all admins
     */
    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> getAllAdmins() {
        List<Person> admins = personService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    /**
     * Get persons by role.
     * GET /api/persons/role/{role}
     * Admin only
     *
     * @param role The role
     * @return List of persons with the specified role
     */
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> getPersonsByRole(@PathVariable Role role) {
        List<Person> persons = personService.getPersonsByRole(role);
        return ResponseEntity.ok(persons);
    }

    /**
     * Search persons by first name.
     * GET /api/persons/search/firstname/{firstName}
     * Admin only
     *
     * @param firstName The first name to search for
     * @return List of matching persons
     */
    @GetMapping("/search/firstname/{firstName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> searchByFirstName(@PathVariable String firstName) {
        List<Person> persons = personService.searchByFirstName(firstName);
        return ResponseEntity.ok(persons);
    }

    /**
     * Search persons by last name.
     * GET /api/persons/search/lastname/{lastName}
     * Admin only
     *
     * @param lastName The last name to search for
     * @return List of matching persons
     */
    @GetMapping("/search/lastname/{lastName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> searchByLastName(@PathVariable String lastName) {
        List<Person> persons = personService.searchByLastName(lastName);
        return ResponseEntity.ok(persons);
    }

    /**
     * Update a person.
     * PUT /api/persons/{id}
     * Admin only
     *
     * @param id     The person ID
     * @param person The updated person data
     * @return The updated person
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Person> updatePerson(@PathVariable Long id, @Valid @RequestBody Person person) {
        Person updatedPerson = personService.updatePerson(id, person);
        return ResponseEntity.ok(updatedPerson);
    }

    /**
     * Update a person's role.
     * PUT /api/persons/{id}/role
     * Admin only
     *
     * @param id The person ID
     * @param role The new role
     * @return The updated person
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Person> updatePersonRole(@PathVariable Long id, @RequestParam Role role) {
        Person updatedPerson = personService.updatePersonRole(id, role);
        return ResponseEntity.ok(updatedPerson);
    }

    /**
     * Delete a person.
     * DELETE /api/persons/{id}
     * Admin only
     *
     * @param id The person ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
}
