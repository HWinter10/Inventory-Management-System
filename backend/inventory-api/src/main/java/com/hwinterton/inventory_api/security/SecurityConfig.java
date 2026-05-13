package com.hwinterton.inventory_api.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/*
Purpose:
- configure Spring Security for endpoint access rules, CORS, database-backed authentication,
  password verification, and JWT request filtering

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
            - use UserDetailsServiceImpl to load users from database
            - use PasswordEncoder to verify password hashes

        - register JwtAuthFilter
            - run before UsernamePasswordAuthenticationFilter
            - check JWT before Spring's default authentication filter

        - build and return SecurityFilterChain

    authenticationProvider():
        - create DaoAuthenticationProvider
        - set UserDetailsServiceImpl
        - set PasswordEncoder
        - return provider

    corsConfigurationSource():
        - allow frontend origin
        - allow HTTP methods
        - allow headers
        - register CORS config for /api/**

*/

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
                .requestMatchers("/api/health", "/api/auth/login").permitAll()
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

    // Method to expose Spring AuthenticationManager so AuthService can verify login credentials
    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
        }
}
