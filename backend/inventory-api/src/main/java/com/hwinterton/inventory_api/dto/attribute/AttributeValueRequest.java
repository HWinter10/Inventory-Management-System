package com.hwinterton.inventory_api.dto.attribute;

/**
 * Request DTO for creating or updating an attribute value.
 *
 * <p>Uses attributeTypeId because each value belongs to an existing
 * attribute type.</p>
 */
public record AttributeValueRequest(
        Long attributeTypeId,
        String value,
        int displayOrder
) {
}