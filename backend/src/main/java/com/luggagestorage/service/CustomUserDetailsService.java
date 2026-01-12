package com.luggagestorage.service;

import com.luggagestorage.model.Person;
import com.luggagestorage.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetailsService implementation for Spring Security.
 * Loads user details from the database for authentication.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PersonRepository personRepository;

    @Autowired
    public CustomUserDetailsService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Person person = personRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return User.builder()
                .username(person.getEmail())
                .password(person.getPasswordHash())
                .authorities(getAuthorities(person))
                .build();
    }

    /**
     * Get user authorities (roles) from Person entity.
     *
     * @param person The person entity
     * @return Collection of granted authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(Person person) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(person.getRole().getAuthority()));
        return authorities;
    }

    /**
     * Load user by ID (useful for token-based authentication).
     *
     * @param id The user ID
     * @return UserDetails
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return User.builder()
                .username(person.getEmail())
                .password(person.getPasswordHash())
                .authorities(getAuthorities(person))
                .build();
    }
}
