package com.medilynk.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig sets up the security configuration for the auth-service application.
 * It defines beans for password encoding and the security filter chain, controlling how HTTP requests are secured.
 */
@Configuration // Marks this class as a source of bean definitions for the Spring context
public class SecurityConfig {

    /**
     * Defines the security filter chain bean, which configures HTTP security for the application.
     * - Permits all incoming HTTP requests without authentication (for development/demo purposes).
     * - Disables CSRF protection for simplicity (not recommended for production).
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Allow all HTTP requests without authentication or authorization
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                // Disable CSRF protection (useful for APIs, but not recommended for browser-based apps)
                .csrf(AbstractHttpConfigurer::disable);
        // Build and return the configured security filter chain
        return http.build();
    }

    /**
     * Defines a bean for password encoding using BCrypt.
     * BCrypt is a strong hashing algorithm recommended for storing passwords securely.
     *
     * @return a PasswordEncoder instance using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Return a BCryptPasswordEncoder for secure password hashing
        return new BCryptPasswordEncoder();
    }
}
