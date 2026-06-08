package com.hwinterton.inventory_api.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hwinterton.inventory_api.dto.auth.ChangePasswordRequest;
import com.hwinterton.inventory_api.dto.auth.LoginRequest;
import com.hwinterton.inventory_api.dto.auth.LoginResponse;
import com.hwinterton.inventory_api.model.User;
import com.hwinterton.inventory_api.repository.UserRepository;
import com.hwinterton.inventory_api.security.JwtUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles authentication workflows including credential verification and JWT response generation.
 * 
 * <p>Coordinated Spring Security authentication, user retrieval, and token generation before
 * returning login response data.</p>
 */
@Slf4j // Lombok: logging feature helper, call replaced need for standard dependencies fields for Slf4j logging
@Service // tells Spring to manage this class as a service-layer bean
public class AuthService {

    // service dependency fields
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    // constructor used by Spring to inject service dependencies
    public AuthService(
            AuthenticationManager authenticationManager,
            UserRepository userRepository,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder) {

        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Login: Authenticates login credentials and returns JWT login response data.
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
        log.info("Login attempt for username: {}", request.username());
        // verifies username/password combo, if invalid Spring auto throws exception
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.username(),
                    request.password()
                )
            );
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for username: {}", request.username());
            throw e;
        }
        // loads FULL user entity from database after authentication, including mustChangePassword
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UsernameNotFoundException("User not found")
        );
        // generates signed JWT containing authenticated user's identity and role
        String token = jwtUtil.generateToken(
            user.getUsername(), 
            user.getRole(),
            user.isMustChangePassword()
        );
        log.info("Login successful for username: {}", user.getUsername());
        // builds and returns DTO to frontend
        return new LoginResponse(
            token,
            user.getUsername(),
            user.getRole().name(),
            user.isMustChangePassword()
        );
    }  

    /**
     * Change password: 
     * 
     * Change password flow:
     *  1. load authenticated user from database
     *  2. verify currnt password matches stored hash
     *  3. hash and store new password
     *  4. set mustChangePassword = false
     *  5. save updated user
     *  6. generate new JWT containing updated mustChangePassword value
     *  7. return LoginResponse containing new token and user info 
     * 
     * @param request current and new password values
     * @return authenticated login response containing a new JWT and updated user information
     */
    public LoginResponse changePassword(String username, ChangePasswordRequest request) {
            log.info("Password change attempt for username: {}", username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // verifies current password against stored BCrypt hash
            if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
                log.warn("Incorrect current password provided for username: {}", username);
                throw new BadCredentialsException("Current password is incorrect");
            }

            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
            user.setMustChangePassword(false);

            User savedUser = userRepository.save(user);

            // generate new JWT after mustChangePassword has been updated
            String token = jwtUtil.generateToken(
                    savedUser.getUsername(),
                    savedUser.getRole(),
                    savedUser.isMustChangePassword()
            );
            log.info("Password changed successfully for username: {}", savedUser.getUsername());
            return new LoginResponse(
                    token,
                    savedUser.getUsername(),
                    savedUser.getRole().name(),
                    savedUser.isMustChangePassword()
            );
        }
}
