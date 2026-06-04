package com.hwinterton.inventory_api.dto.attribute;

/**
 * Response DTO for sending attribute value data back to the frontend.
 *
 * <p>Includes the parent attribute type so values can be displayed with
 * context without exposing the raw entity relationship.</p>
 */
public record AttributeValueResponse(
    Long id,
    Long attributeTypeId,
    String attributeTypeName,
    String value,
    int displayOrder
) {}