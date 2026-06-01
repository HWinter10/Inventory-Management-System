package com.hwinterton.inventory_api.dto.category;

import jakarta.validation.constraints.NotBlank;

/**
 * Response DTO for sending category data back to the frontend
 * 
 * <p>Used so category data can be displayed without expoasing the raw entity</p>
 * <p></p>
 */
public record CategoryResponse(
    Long id, 

    @NotBlank(message = "Must enter a name")
    String name, 
    
    String description) {

}
