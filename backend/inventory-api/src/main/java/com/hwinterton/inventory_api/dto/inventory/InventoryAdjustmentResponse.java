package com.hwinterton.inventory_api.dto.inventory;

import java.time.LocalDateTime;

/**
 * Response DTO for sending inventory adjustment data back to the frontend
 * 
 * <p>Includes readable variant information so the adjustment can be displayed
 * clearly in inventory history screens</p>
 */
public record InventoryAdjustmentResponse(
    Long id,
    Long variantId,
    String variantDisplayName,
    int changeAmount,
    String reason,
    LocalDateTime createdAt
) {
}
