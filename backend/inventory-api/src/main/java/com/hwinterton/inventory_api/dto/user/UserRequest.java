package com.hwinterton.inventory_api.dto.user;

import com.hwinterton.inventory_api.model.Role;

/**
 * Request DTO for creating a user
 * 
 * <p>Used when an owner created a new user and assigns their role</p>
 */
public record UserRequest(
    String username,
    Role role
) {
}
