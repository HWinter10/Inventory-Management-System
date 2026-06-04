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

import com.hwinterton.inventory_api.dto.attribute.AttributeValueRequest;
import com.hwinterton.inventory_api.dto.attribute.AttributeValueResponse;
import com.hwinterton.inventory_api.model.AttributeType;
import com.hwinterton.inventory_api.model.AttributeValue;
import com.hwinterton.inventory_api.repository.AttributeTypeRepository;
import com.hwinterton.inventory_api.repository.AttributeValueRepository;

@ExtendWith(MockitoExtension.class)
public class AttributeValueServiceTest {

    @Mock
    private AttributeTypeRepository attributeTypeRepository;

    @Mock
    private AttributeValueRepository attributeValueRepository;

    private AttributeValueService attributeValueService;

    @BeforeEach
    void setUp() {
        attributeValueService = new AttributeValueService(
                attributeTypeRepository,
                attributeValueRepository
        );
    }

    @Test
    void getAttributeValuesByType_returnsValuesForSelectedType() {
        AttributeType size = new AttributeType();
        size.setId(1L);
        size.setName("Size");

        AttributeValue medium = new AttributeValue();
        medium.setId(1L);
        medium.setAttributeType(size);
        medium.setValue("Medium");
        medium.setDisplayOrder(2);

        AttributeValue large = new AttributeValue();
        large.setId(2L);
        large.setAttributeType(size);
        large.setValue("Large");
        large.setDisplayOrder(3);

        when(attributeTypeRepository.findById(1L))
                .thenReturn(Optional.of(size));

        when(attributeValueRepository.findByAttributeType(size))
                .thenReturn(List.of(medium, large));

        List<AttributeValueResponse> response = attributeValueService.getAttributeValuesByType(1L);

        assertEquals(2, response.size());
        assertEquals("Medium", response.get(0).value());
        assertEquals("Large", response.get(1).value());
        assertEquals("Size", response.get(0).attributeTypeName());

        verify(attributeTypeRepository).findById(1L);
        verify(attributeValueRepository).findByAttributeType(size);
    }

    @Test
    void createAttributeValue_whenValueIsUnique_createsAttributeValue() {
        AttributeType flavor = new AttributeType();
        flavor.setId(1L);
        flavor.setName("Flavor");

        AttributeValueRequest request = new AttributeValueRequest(
                1L,
                "Vanilla",
                1
        );

        when(attributeTypeRepository.findById(1L))
                .thenReturn(Optional.of(flavor));

        when(attributeValueRepository.existsByAttributeTypeAndValue(flavor, "Vanilla"))
                .thenReturn(false);

        when(attributeValueRepository.save(any(AttributeValue.class)))
                .thenAnswer(invocation -> {
                    AttributeValue attributeValue = invocation.getArgument(0);
                    attributeValue.setId(10L);
                    return attributeValue;
                });

        AttributeValueResponse response = attributeValueService.createAttributeValue(request);

        assertEquals(10L, response.id());
        assertEquals(1L, response.attributeTypeId());
        assertEquals("Flavor", response.attributeTypeName());
        assertEquals("Vanilla", response.value());
        assertEquals(1, response.displayOrder());

        verify(attributeTypeRepository).findById(1L);
        verify(attributeValueRepository).existsByAttributeTypeAndValue(flavor, "Vanilla");
        verify(attributeValueRepository).save(any(AttributeValue.class));
    }

    @Test
    void createAttributeValue_whenValueAlreadyExistsUnderType_throwsIllegalArgumentException() {
        AttributeType flavor = new AttributeType();
        flavor.setId(1L);
        flavor.setName("Flavor");

        AttributeValueRequest request = new AttributeValueRequest(
                1L,
                "Vanilla",
                1
        );

        when(attributeTypeRepository.findById(1L))
                .thenReturn(Optional.of(flavor));

        when(attributeValueRepository.existsByAttributeTypeAndValue(flavor, "Vanilla"))
                .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                attributeValueService.createAttributeValue(request)
        );

        verify(attributeTypeRepository).findById(1L);
        verify(attributeValueRepository).existsByAttributeTypeAndValue(flavor, "Vanilla");
        verify(attributeValueRepository, never()).save(any(AttributeValue.class));
    }

    @Test
    void updateAttributeValue_whenValueExists_updatesAttributeValue() {
        AttributeType size = new AttributeType();
        size.setId(1L);
        size.setName("Size");

        AttributeValue existingValue = new AttributeValue();
        existingValue.setId(5L);
        existingValue.setAttributeType(size);
        existingValue.setValue("Medium");
        existingValue.setDisplayOrder(2);

        AttributeValueRequest request = new AttributeValueRequest(
                1L,
                "Large",
                3
        );

        when(attributeValueRepository.findById(5L))
                .thenReturn(Optional.of(existingValue));

        when(attributeTypeRepository.findById(1L))
                .thenReturn(Optional.of(size));

        when(attributeValueRepository.existsByAttributeTypeAndValue(size, "Large"))
                .thenReturn(false);

        when(attributeValueRepository.save(any(AttributeValue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AttributeValueResponse response = attributeValueService.updateAttributeValue(5L, request);

        assertEquals(5L, response.id());
        assertEquals("Large", response.value());
        assertEquals(3, response.displayOrder());
        assertEquals("Size", response.attributeTypeName());

        verify(attributeValueRepository).findById(5L);
        verify(attributeTypeRepository).findById(1L);
        verify(attributeValueRepository).save(existingValue);
    }
}