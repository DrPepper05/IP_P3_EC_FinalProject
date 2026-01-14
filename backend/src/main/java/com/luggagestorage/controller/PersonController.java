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

@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "*")
public class PersonController {

    private final PersonService personService;

    @Autowired
    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> getAllPersons() {
        List<Person> persons = personService.getAllPersons();
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Person> getPersonById(@PathVariable Long id) {
        Person person = personService.getPersonById(id);
        return ResponseEntity.ok(person);
    }

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> getAllCustomers() {
        List<Person> customers = personService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/admins")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> getAllAdmins() {
        List<Person> admins = personService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> getPersonsByRole(@PathVariable Role role) {
        List<Person> persons = personService.getPersonsByRole(role);
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/search/firstname/{firstName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> searchByFirstName(@PathVariable String firstName) {
        List<Person> persons = personService.searchByFirstName(firstName);
        return ResponseEntity.ok(persons);
    }

    @GetMapping("/search/lastname/{lastName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Person>> searchByLastName(@PathVariable String lastName) {
        List<Person> persons = personService.searchByLastName(lastName);
        return ResponseEntity.ok(persons);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Person> updatePerson(@PathVariable Long id, @Valid @RequestBody Person person) {
        Person updatedPerson = personService.updatePerson(id, person);
        return ResponseEntity.ok(updatedPerson);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Person> updatePersonRole(@PathVariable Long id, @RequestParam Role role) {
        Person updatedPerson = personService.updatePersonRole(id, role);
        return ResponseEntity.ok(updatedPerson);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }
}
