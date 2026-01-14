package com.luggagestorage.repository;

import com.luggagestorage.model.Person;
import com.luggagestorage.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Person> findByRole(Role role);

    List<Person> findByFirstNameContainingIgnoreCase(String firstName);

    List<Person> findByLastNameContainingIgnoreCase(String lastName);
}
