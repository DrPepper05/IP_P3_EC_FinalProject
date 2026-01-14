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

    private Collection<? extends GrantedAuthority> getAuthorities(Person person) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(person.getRole().getAuthority()));
        return authorities;
    }

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
