package com.hwinterton.inventory_api.dto.auth;

/**
 * DTO returned after a successful authentication workflow.
 *
 * <p>Used after login and password change because both workflows return
 * the same authentication response data.</p>
 *
 * @param token JWT used for authenticated requests
 * @param username authenticated user's username
 * @param role authenticated user's role
 * @param mustChangePassword whether the user must change their temporary password
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