package com.hwinterton.inventory_api.dto;

/*
Purpose:
- define the data shape sent from the frontend when a user tries to log in

Dependencies:
- none

Pseudocode:
- store the username from the login request
- store the password from the login request

Record Notes:
- Java records automatically generate:
    - private final fields
    - constructor
    - accessors/getters
    - equals()
    - hashCode()
    - toString()
- records are commonly used for DTOs because DTOs are simple immutable data carriers
- password uses String because Spring Security authentication workflows expect String credentials
- plaintext passwords should NEVER be stored in the database
- only password hashes should be stored permanently
*/

public record LoginRequest(
    String username, 
    String password
) {
}
