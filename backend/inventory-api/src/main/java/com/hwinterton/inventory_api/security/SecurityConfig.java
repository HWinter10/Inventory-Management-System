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

/**
/**
 * Configures Spring Security for the backend API.
 * 
 * <p>Defines which endpoints are public, which require authentication, how users are authenticated
 * against the database, and where the JWT filter is added into Spring Security's request flow.</p>
 * 
 * <p>It uses a stateless JWT approach so the backend does not store server-side login sessions.
 * Instead, protected requests are expected to include a JWT in the Authorization header.</p>
 * 
 * Security Flow:
 * <pre>
 * 1. Configure CORS rules
 * 2. Disable CSRF for stateless JWT requests
 * 3. Configure stateless session management
 * 4. Define public and protected endpoints
 * 5. Register authentication provider
 * 6. Run JwtAuthFilter before Spring's default authentication filter
 * 7. Return configured SecurityFilterChain
 * </pre>
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

    // Method: Configure endpoint access, CORS, stateless sessions, authentication provider, and JWT filter
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // allow React fronend to call the backend api
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
            // diable CSRF because it uses steless JWT requests instead of server-side
            .csrf(csrf -> csrf.disable())
            // do not store authenticated users in server-side session
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // permit health check and login without authentication, require auth for all else
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health", "/api/auth/login").permitAll()
                .anyRequest().authenticated()
            )
            // use database-backed authentication with password hash verification
            .authenticationProvider(authenticationProvider())
            // run JWT validation before Springs default username/password authentication filters
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Method: Create authentication provider using database users and password encoder
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // load user details from database during auth
        authProvider.setUserDetailsService(userDetailsService);
        // compare submitted password against stored hashes
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // Method: Configure browser access from React frontend to backend API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // allow requests from local Vite React frontend
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        // allow HTTP methods used by REST API
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        // allow request headers like Authorization and Context-Type
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // apply this CORS config to API routes
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    // Method: Expose Spring AuthenticationManager so AuthService can verify login credentials
    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
        }
}
