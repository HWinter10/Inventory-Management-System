package com.hwinterton.inventory_api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.hwinterton.inventory_api.dto.LoginRequest;
import com.hwinterton.inventory_api.dto.LoginResponse;
import com.hwinterton.inventory_api.model.User;
import com.hwinterton.inventory_api.repository.UserRepository;
import com.hwinterton.inventory_api.security.JwtUtil;

/**
 * Handles authentication workflows including credential verification and JWT response generation.
 * 
 * <p>Coordinated Spring Security authentication, user retrieval, and token generation before
 * returning login response data.</p>
 */
@Service // tells Spring to manage this class as a service-layer bean
public class AuthService {

    // service dependency fields
    private final AuthenticationManager authenticationManager; 
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // constructor used by Spring to inject service dependencies
    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository, JwtUtil jwtUtil) {
            this.authenticationManager = authenticationManager;
            this.userRepository = userRepository;
            this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates login credentials and returns JWT login response data.
     * 
     * Authentication Flow:
     * 1. Verify credentials through Spring Security
     * 2. Load authenticated user from database
     * 3. Generate JWT token
     * 4. Return login response DTO
     * 
     * @param request login credentials from the frontend
     * @return authenticated login response containing JWT and user information
     */
    public LoginResponse login(LoginRequest request){
        // verifies username/password combo, if invalid Spring auto throws exception
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.username(), 
                request.password()
            )
        );
        // loads FULL user entity from database after authentication, including mustChangePassword
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found")
        );
        // generates signed JWT containing authenticated user's identity and role
        String token = jwtUtil.generateToken(
            user.getUsername(), 
            user.getRole()
        );
        // builds and returns DTO to frontend
        return new LoginResponse(
            token,
            user.getUsername(),
            user.getRole().name(),
            user.isMustChangePassword()
        );
    }  
}
