package com.hwinterton.inventory_api.security;

/*
Purpose: configured Spring Security for endpoint access, CORS, database-backed users,
         password verification, and JWT filtering. This essentially helps tell Spring
         to run these congifs before it runs its default security rules. 

Dependencies:
- JwtAuthFilter
- UserDetailsServiceImpl
- PasswordEncoder
- AuthenticationProvider
- SecurityFilterChain
- CorsConfigurationSource

Pseudocode:
    securityFilterChain(http):
        - configure CORS
            - allow React frontend to call backend API
        - define endpoint access rules
            - permit /api/health
            - require authentication for all other endpoints

        - disable CSRF
            - not needed for stateless API requests

        - register authentication provider
            - use UderDetailsServiceImpl to load users from database
            - use PasswordEncoder to verify password hashed

        - register JwtAuthFilter
            - run before UsernamePasswordAuthenticationFilter
            - check JWT before Spring's default authentication filter

        - build and return SecurityFilterChain

    authenticationProvider():
        - create DaoAuthenticationProvider
        - set UserDetailsServiceImpl
        - set PasswordEncoder
        - retunr provider

    corsConfigurationSource():
        - allow frontend origin
        - allow HTTP methods
        - allow headers
        - register CORS config for /api/**

*/

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            UserDetailsServiceImpl userDetailsService,
            PasswordEncoder passwordEncoder) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    // Method - configure endpoint access, authentication provider, JWT filter, and stateless security
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Method - create authentication provider using database users and password encoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // Method - configure browser access from React frontend to backend API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
