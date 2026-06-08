package com.hwinterton.inventory_api.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hwinterton.inventory_api.dto.attribute.AttributeTypeRequest;
import com.hwinterton.inventory_api.dto.attribute.AttributeTypeResponse;
import com.hwinterton.inventory_api.model.AttributeType;
import com.hwinterton.inventory_api.repository.AttributeTypeRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for attribute type business logic.
 *
 * <p>Handles creating, retrieving, and updating attribute types such as
 * Size, Color, Flavor, or Gender.</p>
 */
@Slf4j // Lombok: logging feature helper, call replaced need for standard dependencies fields for Slf4j logging
@Service
public class AttributeTypeService {

    private final AttributeTypeRepository attributeTypeRepository;

    public AttributeTypeService(AttributeTypeRepository attributeTypeRepository) {
        this.attributeTypeRepository = attributeTypeRepository;
    }

    /**
     * Method: retrieves all attribute types.
     *
     * @return a list of attribute type response DTOs
     */
    @Transactional(readOnly = true) // protects workflow as a whole, as in it succeeds or fails as one unit
    public List<AttributeTypeResponse> getAllAttributeTypes() {
        log.info("Fetching all attribute types");
        return attributeTypeRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Method: retrieves one attribute type by ID.
     *
     * @param id the attribute type ID
     * @return the matching attribute type as a response DTO
     */
    @Transactional(readOnly = true) // protected workflow
    public AttributeTypeResponse getAttributeTypeById(Long id) {
        log.info("Fetching attribute type with id: {}", id);
        AttributeType attributeType = findAttributeTypeById(id);

        return toResponse(attributeType);
    }

    /**
     * Method: creates a new attribute type.
     *
     * <p>Prevents duplicate attribute type names.</p>
     *
     * @param request the attribute type data sent from the frontend
     * @return the created attribute type as a response DTO
     */
    @Transactional // protected workflow
    public AttributeTypeResponse createAttributeType(AttributeTypeRequest request) {
        log.info("Attempting to create attribute type with name: {}", request.name());
        if (attributeTypeRepository.existsByName(request.name())) {
            log.warn("Duplicate attribute type name attempted: {}", request.name());
            throw new IllegalArgumentException("Attribute type name already exists");
        }

        AttributeType attributeType = new AttributeType();
        attributeType.setName(request.name());
        attributeType.setDescription(request.description());

        AttributeType savedAttributeType = attributeTypeRepository.save(attributeType);
        log.info("Attribute type created successfully with id: {}", savedAttributeType.getId());
        
        return toResponse(savedAttributeType);
    }

    /**
     * Method: updates an existing attribute type.
     *
     * <p>Allows the name and description to be changed while preventing
     * duplicate attribute type names.</p>
     *
     * @param id the attribute type ID
     * @param request the updated attribute type data sent from the frontend
     * @return the updated attribute type as a response DTO
     */
    @Transactional // protected workflow
    public AttributeTypeResponse updateAttributeType(Long id, AttributeTypeRequest request) {
        log.info("Attempting to update attribute type with id: {}", id);

        AttributeType attributeType = findAttributeTypeById(id);

        if (!attributeType.getName().equals(request.name())
                && attributeTypeRepository.existsByName(request.name())) {

            log.warn("Duplicate attribute type name attempted during update: {}", request.name());
            throw new IllegalArgumentException("Attribute type name already exists");
        }

        attributeType.setName(request.name());
        attributeType.setDescription(request.description());

        AttributeType updatedAttributeType = attributeTypeRepository.save(attributeType);
        log.info("Attribute type updated successfully with id: {}", updatedAttributeType.getId());

        return toResponse(updatedAttributeType);
    }

    // Method: finds an attribute type once so the not-found logic stays consistent.
    private AttributeType findAttributeTypeById(Long id) {
        return attributeTypeRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Attribute type not found with id: {}", id);
                    return new RuntimeException("Attribute type not found");
                });
    }

    // Method: converts the AttributeType entity into the response shape used by the frontend.
    private AttributeTypeResponse toResponse(AttributeType attributeType) {
        return new AttributeTypeResponse(
                attributeType.getId(),
                attributeType.getName(),
                attributeType.getDescription()
        );
    }
}