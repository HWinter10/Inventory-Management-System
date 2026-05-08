package com.hwinterton.inventory_api.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hwinterton.inventory_api.service.AuthService;

/*
Purpose:
- expose authentication endpoints to the frontend

Pseudocode:
- receive HTTP authentication requests
- pass login data to AuthService
- return authentication responses back to the client
*/

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // controller dependency field used to pass authentication work to AuthService
    private final AuthService authService;

    // constructor for passing AuthService into controller
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
}
