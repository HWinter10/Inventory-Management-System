package com.hwinterton.inventory_api.dto;

/*
Purpose:
- define the data shape returned to the frontend after a successful login

Dependencies:
- none

Pseudocode:
- store the JWT created by the backend
- store the username for frontend display or state
- store the user's role for frontend permission decisions
- store whether the user must change their temporary password

Record Notes:
- Java records automatically generate:
    - private final fields
    - constructor
    - accessors/getters
    - equals()
    - hashCode()
    - toString()
- records work well for DTOs because DTOs should remain lightweight and immutable
*/

public record LoginResponse(
    String token, 
    String username, 
    String role, 
    boolean mustChangePassword
) {
    // note: record file types don't use getters traditionally. So instead
    //       of request.getUsername() we use request.username()
}