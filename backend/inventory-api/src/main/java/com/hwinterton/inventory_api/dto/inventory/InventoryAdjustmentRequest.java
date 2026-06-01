package com.hwinterton.inventory_api.dto.inventory;

/**
 * Request DTO for recording an inventory adjustment
 * 
 * <p>Used when stock needs to be increased or decreased, such as after a sale, 
 * restock, correction, damage, or manual adjustment</p>
 */
public record InventoryAdjustmentRequest(
    Long variantId,
    int changeAmount,
    String reason
) {

}
