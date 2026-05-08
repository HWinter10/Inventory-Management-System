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

/*
Purpose:
- handle the login workflow by verifying credentials, loading the user, and returning JWT login data

Dependencies:
- AuthenticationManager
- UserRepository
- JwtUtil
- LoginRequest DTO
- LoginResponse DTO

Pseudocode:
  login(LoginRequest request):
    - receive LoginRequest from controller
    - pass request username and password to AuthenticationManager
        - if credentials are invalid, Spring throws an authentication exception
    - load user from UserRepository by username
        - if user is not found, throw UsernameNotFoundException
    - generate JWT using JwtUtil with username and role
    - build LoginResponse containing:
        - token
        - username
        - role
        - mustChangePassword
    - return LoginResponse to controller
*/

@Service // tells Spring to manage this class as a service-layer bean
public class AuthService {

    // the following fields hold references to the objects AuthService needs.
    // in plain Java, I might create them myself like: JwtUtil jwtUtil = new JwtUtil();
    // but in Spring, I do not create them manually since Spring creates the objects 
    // and gives them to this class through the constructor (done behind the scenes).
    // this is one example of IoC (Inversion of control) 
    private final AuthenticationManager authenticationManager; 
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    // constructor injection: Spring provides dependencies when AuthService is created
    public AuthService(
        AuthenticationManager authenticationManager, 
        UserRepository userRepository,
        JwtUtil jwtUtil
    ) {
            this.authenticationManager = authenticationManager;
            this.userRepository = userRepository;
            this.jwtUtil = jwtUtil;
    }

    // method to authenticate login request and return data to frontend 
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
