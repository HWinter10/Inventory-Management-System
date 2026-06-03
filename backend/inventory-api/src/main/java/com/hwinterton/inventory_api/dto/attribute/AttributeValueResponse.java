package com.hwinterton.inventory_api.dto.attribute;

public record AttributeValueResponse(
    Long id,
    String value,
    Long attributeTypeId,
    String attributeTypeName
) {}