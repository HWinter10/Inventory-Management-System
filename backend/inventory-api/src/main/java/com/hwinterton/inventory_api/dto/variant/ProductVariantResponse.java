package com.hwinterton.inventory_api.dto.variant;

/**
 * Response DTO for sending product variant data back to the frontend
 * 
 * <p>Includes productName so the frontend can display the variant with its
 * parent product information without making another request</p>
 */
public record ProductVariantResponse(
    Long id,
    Long productId,
    String productName,
    String sku,
    String displayName,
    int quantityOnHand,
    int lowStockThreshold,
    boolean active
) {
}
