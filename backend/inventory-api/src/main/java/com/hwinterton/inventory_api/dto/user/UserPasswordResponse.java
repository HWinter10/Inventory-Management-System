package com.hwinterton.inventory_api.dto.user;

/**
 * Response DTO for temporary password actions.
 *
 * <p>Used when an owner creates a user or resets a user's password and the
 * temporary password needs to be shown once.</p>
 */
public record UserPasswordResponse(
        Long userId,
        String username,
        String temporaryPassword
) {
}