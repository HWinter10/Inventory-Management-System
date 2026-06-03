package com.hwinterton.inventory_api.dto.attribute;

public record VariantAttributeResponse(
    Long id,
    Long attributeValueId,
    String value,
    String attributeTypeName
) {}