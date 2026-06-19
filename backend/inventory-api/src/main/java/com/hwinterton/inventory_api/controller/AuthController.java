package com.hwinterton.inventory_api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hwinterton.inventory_api.dto.auth.ChangePasswordRequest;
import com.hwinterton.inventory_api.dto.auth.LoginRequest;
import com.hwinterton.inventory_api.dto.auth.LoginResponse;
import com.hwinterton.inventory_api.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for authentication endpoints under /api/auth
 * 
 * <p>Receive authentication requests from the frontend and delegated
 * login and password-change workflow to AutheService</p>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // dependency field used to pass authentication work to AuthService
    private final AuthService authService;

    /**
     * Receives login requests from frontend and returns JWT login data
     * 
     * @param request login from frontend
     * @return JWT login response containing token and user information
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Receives password change requests and returns updated JWT login data.
     *
     * @param request current and new password values
     * @return JWT login response with mustChangePassword set to false
     */
    @PostMapping("/change-password")
    public ResponseEntity<LoginResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                authService.changePassword(authentication.getName(), request)
        );
    }
}
