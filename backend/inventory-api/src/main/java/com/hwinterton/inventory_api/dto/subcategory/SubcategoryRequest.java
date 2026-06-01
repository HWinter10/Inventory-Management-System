package com.hwinterton.inventory_api.dto.subcategory;

/**
 * Request DTO for creating or updating a subcategory
 * 
 * <p>Uses categoryId because the frontend selectrs an existing category 
 * instead of sending the full Category object</p>
 */
public record SubcategoryRequest(
    String name,
    String description,
    Long categoryId
) {
}
