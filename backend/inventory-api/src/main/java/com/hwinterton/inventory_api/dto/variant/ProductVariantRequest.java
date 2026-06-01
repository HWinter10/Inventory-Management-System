package com.hwinterton.inventory_api.dto.variant;

/**
 * Request DTO for creating or updating a product variant
 * 
 * <p>Uses productId because the frontend selects an existing product instead
 * of sending the full product object</p>
 */
public record ProductVariantRequest(
    Long productId,
    String sku,
    String displayName,
    int quantityOnHand,
    int lowStockThreshold
) {
}
