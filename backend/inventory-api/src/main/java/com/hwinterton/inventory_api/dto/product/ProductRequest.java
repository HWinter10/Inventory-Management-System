package com.hwinterton.inventory_api.dto.product;

/**
 * Request DTO for creating or updating a product
 * 
 * <p>Uses subcategoryId because the frontend selects and existing subcategory
 * instead of sending the full Subcategory object</p>
 */
public record ProductRequest(
    String name,
    String description,
    Long subcategoryId
) {
}
