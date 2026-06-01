package com.hwinterton.inventory_api.dto.subcategory;

/**
 * Response DTO for sending subcategory data back to the frontend
 * 
 * <p>Includes the parent category's ID and name so the frontend can 
 * display useful category information without making another request</p>
 */
public record SubcategoryResponse(
    Long id,
    String name,
    String description,
    Long categoryId,
    String categoryName
) {
}
