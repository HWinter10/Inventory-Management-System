package com.hwinterton.inventory_api.dto.user;

import com.hwinterton.inventory_api.model.Role;

/**
 * Response DTO for sending user account data back to the frontend.
 *
 * <p>Does not include password hash or other sensitive authentication data.</p>
 */
public record UserResponse(
        Long id,
        String username,
        Role role,
        boolean mustChangePassword,
        boolean active
) {
}
