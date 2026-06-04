package com.hwinterton.inventory_api.dto.attribute;

/**
 * Request DTO for creating or updating an attribute type.
 *
 * <p>Used for attribute groups such as Size, Color, Flavor, or Gender.</p>
 */
public record AttributeTypeRequest(
        String name,
        String description
) {
}
