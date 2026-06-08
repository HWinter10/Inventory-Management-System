package com.hwinterton.inventory_api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hwinterton.inventory_api.dto.attribute.AttributeValueRequest;
import com.hwinterton.inventory_api.dto.attribute.AttributeValueResponse;
import com.hwinterton.inventory_api.model.AttributeType;
import com.hwinterton.inventory_api.model.AttributeValue;
import com.hwinterton.inventory_api.repository.AttributeTypeRepository;
import com.hwinterton.inventory_api.repository.AttributeValueRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for attribute value business logic.
 *
 * <p>Handles creating, retrieving, and updating attribute values such as
 * Medium, Pink, Vanilla, or Women's.</p>
 */
@Slf4j // Lombok: logging feature helper, call replaced need for standard dependencies fields for Slf4j logging
@Service
public class AttributeValueService {

    private final AttributeTypeRepository attributeTypeRepository;
    private final AttributeValueRepository attributeValueRepository;

    public AttributeValueService(
            AttributeTypeRepository attributeTypeRepository,
            AttributeValueRepository attributeValueRepository
    ) {
        this.attributeTypeRepository = attributeTypeRepository;
        this.attributeValueRepository = attributeValueRepository;
    }

    /**
     * Method: retrieves all attribute values.
     *
     * @return a list of attribute value response DTOs
     */
    @Transactional(readOnly = true) // protects workflow as a whole, as in it succeeds or fails as one unit
    public List<AttributeValueResponse> getAllAttributeValues() {
        log.info("Fetching all attribute values");

        return attributeValueRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Method: retrieves one attribute value by ID.
     *
     * @param id the attribute value ID
     * @return the matching attribute value as a response DTO
     */
    @Transactional(readOnly = true) // protects workflow
    public AttributeValueResponse getAttributeValueById(Long id) {
        log.info("Fetching attribute value with id: {}", id);
        
        AttributeValue attributeValue = findAttributeValueById(id);

        return toResponse(attributeValue);
    }

    /**
     * Method: retrieves all values for one attribute type.
     *
     * @param attributeTypeId the attribute type ID
     * @return a list of attribute values under the selected type
     */
    @Transactional(readOnly = true) // protects workflow
    public List<AttributeValueResponse> getAttributeValuesByType(Long attributeTypeId) {
        log.info("Fetching attribute values for attribute type id: {}", attributeTypeId);
        
        AttributeType attributeType = findAttributeTypeById(attributeTypeId);

        return attributeValueRepository.findByAttributeType(attributeType)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Method: creates a new attribute value under an existing attribute type.
     *
     * <p>Prevents duplicate values within the same attribute type.</p>
     *
     * @param request the attribute value data sent from the frontend
     * @return the created attribute value as a response DTO
     */
    @Transactional // protects workflow
    public AttributeValueResponse createAttributeValue(AttributeValueRequest request) {
        log.info("Attempting to create attribute value: {} under attribute type id: {}", request.value(), request.attributeTypeId());
        
        AttributeType attributeType = findAttributeTypeById(request.attributeTypeId());

        if (attributeValueRepository.existsByAttributeTypeAndValue(attributeType, request.value())) {
            log.warn("Duplicate attribute value attempted: {} under attribute type id: {}", request.value(), request.attributeTypeId());
            throw new IllegalArgumentException("Attribute value already exists under this attribute type");
        }

        AttributeValue attributeValue = new AttributeValue();
        attributeValue.setAttributeType(attributeType);
        attributeValue.setValue(request.value());
        attributeValue.setDisplayOrder(request.displayOrder());

        AttributeValue savedAttributeValue = attributeValueRepository.save(attributeValue);
        log.info("Attribute value created successfully with id: {}", savedAttributeValue.getId());

        return toResponse(savedAttributeValue);
    }

    /**
     * Method: updates an existing attribute value.
     *
     * <p>Allows the value, display order, and parent attribute type to be changed
     * while preventing duplicate values within the selected type.</p>
     *
     * @param id the attribute value ID
     * @param request the updated attribute value data sent from the frontend
     * @return the updated attribute value as a response DTO
     */
    @Transactional // protects workflow
    public AttributeValueResponse updateAttributeValue(Long id, AttributeValueRequest request) {
        log.info("Attempting to update attribute value with id: {}", id);

        AttributeValue attributeValue = findAttributeValueById(id);
        AttributeType attributeType = findAttributeTypeById(request.attributeTypeId());

        boolean typeChanged = !attributeValue.getAttributeType().getId().equals(attributeType.getId());
        boolean valueChanged = !attributeValue.getValue().equals(request.value());

        if ((typeChanged || valueChanged)
                && attributeValueRepository.existsByAttributeTypeAndValue(attributeType, request.value())) {
            log.warn("Duplicate attribute value attempted during update: {} under attribute type id: {}", request.value(), request.attributeTypeId());
            throw new IllegalArgumentException("Attribute value already exists under this attribute type");
        }

        attributeValue.setAttributeType(attributeType);
        attributeValue.setValue(request.value());
        attributeValue.setDisplayOrder(request.displayOrder());

        AttributeValue updatedAttributeValue = attributeValueRepository.save(attributeValue);
        log.info("Attribute value updated successfully with id: {}", updatedAttributeValue.getId());

        return toResponse(updatedAttributeValue);
    }

    // Method: finds an attribute type once so the not-found logic stays consistent.
    private AttributeType findAttributeTypeById(Long id) {
        return attributeTypeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Attribute type not found with id: {}", id);
                    return new RuntimeException("Attribute type not found");
                });
    }

    // Method: finds an attribute value once so the not-found logic stays consistent.
    private AttributeValue findAttributeValueById(Long id) {
        return attributeValueRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Attribute value not found with id: {}", id);              
                    return new RuntimeException("Attribute value not found");
                });
    }

    // Method: converts the AttributeValue entity into the response shape used by the frontend.
    private AttributeValueResponse toResponse(AttributeValue attributeValue) {
        return new AttributeValueResponse(
                attributeValue.getId(),
                attributeValue.getAttributeType().getId(),
                attributeValue.getAttributeType().getName(),
                attributeValue.getValue(),
                attributeValue.getDisplayOrder()
        );
    }
}