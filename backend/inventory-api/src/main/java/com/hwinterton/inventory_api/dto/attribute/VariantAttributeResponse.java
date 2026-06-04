package com.hwinterton.inventory_api.dto.attribute;

/**
 * Response DTO for sending variant attribute data back to the frontend.
 *
 * <p>Represents one attribute value attached to a product variant, such as
 * Size = Medium, Color = Pink, or Flavor = Vanilla.</p>
 */
public record VariantAttributeResponse(
    Long id,
    Long attributeValueId,
    String value,
    String attributeTypeName
) {}