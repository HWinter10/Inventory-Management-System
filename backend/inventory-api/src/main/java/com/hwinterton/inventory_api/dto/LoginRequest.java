package com.hwinterton.inventory_api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO used to receive login credentials from the frontend.
 * 
 * <p>Validation constraints prevent null. empty or whitespace only values
 * before the request reaches the service layer.</p>
 * 
 * @param username 
 * @param password
 */
public record LoginRequest(
    
    @NotBlank(message = "Username required")
    String username, 

    @NotBlank(message = "Password required")
    String password
) {
}
