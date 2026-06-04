package com.hwinterton.inventory_api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hwinterton.inventory_api.dto.attribute.VariantAttributeResponse;
import com.hwinterton.inventory_api.model.AttributeValue;
import com.hwinterton.inventory_api.model.ProductVariant;
import com.hwinterton.inventory_api.model.VariantAttribute;
import com.hwinterton.inventory_api.repository.AttributeValueRepository;
import com.hwinterton.inventory_api.repository.ProductVariantRepository;
import com.hwinterton.inventory_api.repository.VariantAttributeRepository;

/**
 * Service for variant attribute business logic.
 *
 * <p>Handles retrieving, adding, and removing attribute values attached to
 * product variants.</p>
 */
@Service
public class VariantAttributeService {

    private final VariantAttributeRepository variantAttributeRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AttributeValueRepository attributeValueRepository;

    public VariantAttributeService(
            VariantAttributeRepository variantAttributeRepository,
            ProductVariantRepository productVariantRepository,
            AttributeValueRepository attributeValueRepository
    ) {
        this.variantAttributeRepository = variantAttributeRepository;
        this.productVariantRepository = productVariantRepository;
        this.attributeValueRepository = attributeValueRepository;
    }

    /**
     * Method: retrieves all attributes attached to a product variant.
     *
     * @param variantId the product variant ID
     * @return a list of variant attribute response DTOs
     */
    @Transactional(readOnly = true)
    public List<VariantAttributeResponse> getAttributesForVariant(Long variantId) {
        ProductVariant productVariant = findProductVariantById(variantId);

        return variantAttributeRepository.findByProductVariant(productVariant)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Method: adds an attribute value to a product variant.
     *
     * <p>Prevents the exact same value from being added twice and prevents
     * multiple values from the same attribute type being attached to one variant.</p>
     *
     * @param variantId the product variant ID
     * @param attributeValueId the attribute value ID
     * @return the saved variant attribute as a response DTO
     */
    @Transactional
    public VariantAttributeResponse addAttributeToVariant(Long variantId, Long attributeValueId) {
        ProductVariant productVariant = findProductVariantById(variantId);
        AttributeValue attributeValue = findAttributeValueById(attributeValueId);

        if (variantAttributeRepository.existsByProductVariantAndAttributeValue(productVariant, attributeValue)) {
            throw new IllegalStateException("Attribute value is already attached to this variant");
        }

        // a variant should not have two values from the same attribute type.
        boolean sameAttributeTypeExists = variantAttributeRepository.findByProductVariant(productVariant)
                .stream()
                .anyMatch(existingAttribute ->
                        existingAttribute.getAttributeValue()
                                .getAttributeType()
                                .getId()
                                .equals(attributeValue.getAttributeType().getId())
                );

        if (sameAttributeTypeExists) {
            throw new IllegalStateException("Variant already has a value for this attribute type");
        }

        VariantAttribute variantAttribute = new VariantAttribute();
        variantAttribute.setProductVariant(productVariant);
        variantAttribute.setAttributeValue(attributeValue);

        VariantAttribute savedVariantAttribute = variantAttributeRepository.save(variantAttribute);

        return toResponse(savedVariantAttribute);
    }

    /**
     * Method: removes an attribute value from a product variant.
     *
     * <p>This is a hard delete because VariantAttribute is only a linking record.</p>
     *
     * @param variantAttributeId the variant attribute ID
     */
    @Transactional
    public void removeAttributeFromVariant(Long variantAttributeId) {
        VariantAttribute variantAttribute = variantAttributeRepository.findById(variantAttributeId)
                .orElseThrow(() -> new RuntimeException("Variant attribute not found"));

        variantAttributeRepository.delete(variantAttribute);
    }

    // Method: finds a product variant once so the not-found logic stays consistent.
    private ProductVariant findProductVariantById(Long id) {
        return productVariantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));
    }

    // Method: finds an attribute value once so the not-found logic stays consistent.
    private AttributeValue findAttributeValueById(Long id) {
        return attributeValueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Attribute value not found"));
    }

    // Method: converts the VariantAttribute entity into the response shape used by the frontend.
    private VariantAttributeResponse toResponse(VariantAttribute variantAttribute) {
        AttributeValue attributeValue = variantAttribute.getAttributeValue();

        return new VariantAttributeResponse(
                variantAttribute.getId(),
                attributeValue.getId(),
                attributeValue.getValue(),
                attributeValue.getAttributeType().getName()
        );
    }
}