package com.luggagestorage.config;

import com.luggagestorage.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

/**
 * Spring Security configuration with JWT authentication.
 * Implements password hashing with BCrypt and role-based authorization.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Password encoder bean using BCrypt.
     * Part of the security requirement: passwords must be hashed, not stored in plain text.
     *
     * @return BCrypt password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication manager bean.
     *
     * @return Authentication manager
     * @throws Exception if configuration fails
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * Configure authentication manager with custom user details service and password encoder.
     *
     * @param auth Authentication manager builder
     * @throws Exception if configuration fails
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    /**
     * Configure HTTP security with JWT authentication and authorization rules.
     * Implements role-based access control (RBAC).
     *
     * @param http HTTP security configuration
     * @throws Exception if configuration fails
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf().disable()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // Public endpoints (no authentication required)
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/lockers", "/api/lockers/**").permitAll()
                .antMatchers("/ws/**").permitAll()  // Allow WebSocket connections
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()  // Allow Swagger UI

                // Admin-only endpoints
                .antMatchers(HttpMethod.POST, "/api/lockers/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/lockers/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/lockers/**").hasRole("ADMIN")
                .antMatchers("/api/persons/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.GET, "/api/bookings").hasRole("ADMIN")

                // Customer and Admin endpoints
                .antMatchers("/api/bookings/**").hasAnyRole("CUSTOMER", "ADMIN")

                // All other endpoints require authentication
                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                });

        // Add JWT authentication filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
