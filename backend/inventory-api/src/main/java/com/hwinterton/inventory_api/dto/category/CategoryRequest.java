package com.hwinterton.inventory_api.dto.category;

/**
 * Request DTO for creating or updating a category
 * 
 * <p>Used when frontend sends category form data to the backend</p>
 */
public record CategoryRequest(
    String name, 
    String description) {
    /*
     * Record note: getters are not used the same with records, 
     *          ex: request.name() instead of request.getName().
     */
}