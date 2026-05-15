package com.hwinterton.inventory_api.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.hwinterton.inventory_api.dto.LoginRequest;
import com.hwinterton.inventory_api.dto.LoginResponse;
import com.hwinterton.inventory_api.model.Role;
import com.hwinterton.inventory_api.model.User;
import com.hwinterton.inventory_api.repository.UserRepository;
import com.hwinterton.inventory_api.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_validCredentials_returnLoginResponse() {
        LoginRequest request = new LoginRequest("owner", "password123");

        User user = new User();
        user.setUsername("owner");
        user.setRole(Role.OWNER);
        user.setMustChangePassword(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        when(userRepository.findByUsername("owner"))
                .thenReturn(Optional.of(user));

        when(jwtUtil.generateToken("owner", Role.OWNER))
                .thenReturn("fake-jwt-token");
            
        LoginResponse response = authService.login(request);

        assertEquals("fake-jwt-token", response.token());
        assertEquals("owner", response.username());
        assertEquals("OWNER", response.role());
        assertEquals(false, response.mustChangePassword());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("owner");
        verify(jwtUtil).generateToken("owner", Role.OWNER);
    }

    @Test
    void login_invalidCredentials_throwsException() {
        LoginRequest request = new LoginRequest("owner", "wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByUsername(any());
        verify(jwtUtil, never()).generateToken(any(), any());
    }

}