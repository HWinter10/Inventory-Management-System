package com.hwinterton.inventory_api.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hwinterton.inventory_api.dto.attribute.AttributeTypeRequest;
import com.hwinterton.inventory_api.dto.attribute.AttributeTypeResponse;
import com.hwinterton.inventory_api.model.AttributeType;
import com.hwinterton.inventory_api.repository.AttributeTypeRepository;

@ExtendWith(MockitoExtension.class)
public class AttributeTypeServiceTest {

    @Mock
    private AttributeTypeRepository attributeTypeRepository;

    private AttributeTypeService attributeTypeService;

    @BeforeEach
    void setUp() {
        attributeTypeService = new AttributeTypeService(attributeTypeRepository);
    }

    @Test
    void getAllAttributeTypes_returnsListOfAttributeTypeResponses() {
        AttributeType size = new AttributeType();
        size.setId(1L);
        size.setName("Size");
        size.setDescription("Product size options");

        AttributeType color = new AttributeType();
        color.setId(2L);
        color.setName("Color");
        color.setDescription("Product color options");

        when(attributeTypeRepository.findAll())
                .thenReturn(List.of(size, color));

        List<AttributeTypeResponse> response = attributeTypeService.getAllAttributeTypes();

        assertEquals(2, response.size());
        assertEquals("Size", response.get(0).name());
        assertEquals("Color", response.get(1).name());

        verify(attributeTypeRepository).findAll();
    }

    @Test
    void createAttributeType_whenNameIsUnique_createsAttributeType() {
        AttributeTypeRequest request = new AttributeTypeRequest(
                "Flavor",
                "Supplement flavor options"
        );

        when(attributeTypeRepository.existsByName("Flavor"))
                .thenReturn(false);

        when(attributeTypeRepository.save(any(AttributeType.class)))
                .thenAnswer(invocation -> {
                    AttributeType attributeType = invocation.getArgument(0);
                    attributeType.setId(1L);
                    return attributeType;
                });

        AttributeTypeResponse response = attributeTypeService.createAttributeType(request);

        assertEquals(1L, response.id());
        assertEquals("Flavor", response.name());
        assertEquals("Supplement flavor options", response.description());

        verify(attributeTypeRepository).existsByName("Flavor");
        verify(attributeTypeRepository).save(any(AttributeType.class));
    }

    @Test
    void createAttributeType_whenNameExists_throwsIllegalArgumentException() {
        AttributeTypeRequest request = new AttributeTypeRequest(
                "Size",
                "Product size options"
        );

        when(attributeTypeRepository.existsByName("Size"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                attributeTypeService.createAttributeType(request)
        );

        verify(attributeTypeRepository).existsByName("Size");
        verify(attributeTypeRepository, never()).save(any(AttributeType.class));
    }

    @Test
    void updateAttributeType_whenAttributeTypeExists_updatesAttributeType() {
        AttributeType existingType = new AttributeType();
        existingType.setId(1L);
        existingType.setName("Size");
        existingType.setDescription("Old description");

        AttributeTypeRequest request = new AttributeTypeRequest(
                "Size",
                "Updated size options"
        );

        when(attributeTypeRepository.findById(1L))
                .thenReturn(Optional.of(existingType));

        when(attributeTypeRepository.save(any(AttributeType.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AttributeTypeResponse response = attributeTypeService.updateAttributeType(1L, request);

        assertEquals(1L, response.id());
        assertEquals("Size", response.name());
        assertEquals("Updated size options", response.description());

        verify(attributeTypeRepository).findById(1L);
        verify(attributeTypeRepository).save(existingType);
    }
}