package com.hwinterton.inventory_api.dto.product;

/**
 * Response DTO for sending product data back to the frontend
 * 
 * <p>Includes the parent subcategory's ID and name so the frontend can 
 * display useful subcategory information without making another request</p>
 */
public record ProductResponse(
    Long id,
    String name,
    String description,
    boolean active,
    Long subcategoryId,
    String subcategoryName
) {

}
