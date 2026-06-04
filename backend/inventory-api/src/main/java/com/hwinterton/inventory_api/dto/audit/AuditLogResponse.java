package com.hwinterton.inventory_api.dto.audit;

import java.time.LocalDateTime;

/**
 * Response DTO for sending audit log data back to the frontend.
 *
 * <p>Includes user display information without exposing the full User entity.</p>
 */
public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String actorUsername,
        String action,
        String details,
        LocalDateTime createdAt
) {
}