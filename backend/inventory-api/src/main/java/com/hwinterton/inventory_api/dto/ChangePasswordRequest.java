package com.hwinterton.inventory_api.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO used to receive password change credentials from the frontend.
 *
 * <p>The username is not included because the authenticated user should come
 * from the security context, not from client-provided request data.</p>
 *
 * @param currentPassword user's current password
 * @param newPassword user's new password
 */
public record ChangePasswordRequest(

    @NotBlank(message = "Current password required")
    String currentPassword,

    @NotBlank(message = "New password required")
    String newPassword
) {
}
