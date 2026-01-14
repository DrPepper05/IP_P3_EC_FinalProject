package com.luggagestorage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * CORS (Cross-Origin Resource Sharing) configuration.
 * Allows frontend application running on different port to communicate with backend API.
 */
@Configuration
public class CorsConfig {

    /**
     * Configure CORS filter to allow cross-origin requests from frontend.
     *
     * @return CORS filter bean
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers, etc.)
        config.setAllowCredentials(true);

        // Allow frontend origins (React dev server typically runs on port 3000)
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001",
            "http://127.0.0.1:3000",
            "http://127.0.0.1:3001"
        ));

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("*"));

        // Expose authorization header to frontend
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        config.setMaxAge(3600L);
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
