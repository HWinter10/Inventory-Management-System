package com.hwinterton.inventory_api.dto.attribute;

/**
 * Response DTO for sending attribute type data back to the frontend.
 *
 * <p>Represents an attribute group such as Size, Color, Flavor, or Gender.</p>
 */
public record AttributeTypeResponse(
    Long id,
    String name,
    String description
) {}
