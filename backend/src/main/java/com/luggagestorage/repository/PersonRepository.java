package com.luggagestorage.repository;

import com.luggagestorage.model.Person;
import com.luggagestorage.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Person entity.
 * Provides data access methods for user management.
 */
@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    /**
     * Find a person by email address.
     *
     * @param email The email address
     * @return Optional containing the person if found
     */
    Optional<Person> findByEmail(String email);

    /**
     * Check if a person exists with the given email.
     *
     * @param email The email address
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find all persons with a specific role.
     *
     * @param role The role to filter by
     * @return List of persons with the specified role
     */
    List<Person> findByRole(Role role);

    /**
     * Find persons by first name.
     *
     * @param firstName The first name
     * @return List of persons with matching first name
     */
    List<Person> findByFirstNameContainingIgnoreCase(String firstName);

    /**
     * Find persons by last name.
     *
     * @param lastName The last name
     * @return List of persons with matching last name
     */
    List<Person> findByLastNameContainingIgnoreCase(String lastName);
}
