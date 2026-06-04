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

import com.hwinterton.inventory_api.dto.attribute.VariantAttributeResponse;
import com.hwinterton.inventory_api.model.AttributeType;
import com.hwinterton.inventory_api.model.AttributeValue;
import com.hwinterton.inventory_api.model.ProductVariant;
import com.hwinterton.inventory_api.model.VariantAttribute;
import com.hwinterton.inventory_api.repository.AttributeValueRepository;
import com.hwinterton.inventory_api.repository.ProductVariantRepository;
import com.hwinterton.inventory_api.repository.VariantAttributeRepository;

@ExtendWith(MockitoExtension.class)
public class VariantAttributeServiceTest {

    @Mock
    private VariantAttributeRepository variantAttributeRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private AttributeValueRepository attributeValueRepository;

    private VariantAttributeService variantAttributeService;

    @BeforeEach
    void setUp() {
        variantAttributeService = new VariantAttributeService(
                variantAttributeRepository,
                productVariantRepository,
                attributeValueRepository
        );
    }

    @Test
    void getAttributesForVariant_returnsListOfVariantAttributeResponses() {
        ProductVariant variant = new ProductVariant();
        variant.setId(1L);

        AttributeType sizeType = new AttributeType();
        sizeType.setId(1L);
        sizeType.setName("Size");

        AttributeValue medium = new AttributeValue();
        medium.setId(10L);
        medium.setValue("Medium");
        medium.setAttributeType(sizeType);

        VariantAttribute variantAttribute = new VariantAttribute();
        variantAttribute.setId(100L);
        variantAttribute.setProductVariant(variant);
        variantAttribute.setAttributeValue(medium);

        when(productVariantRepository.findById(1L))
                .thenReturn(Optional.of(variant));

        when(variantAttributeRepository.findByProductVariant(variant))
                .thenReturn(List.of(variantAttribute));

        List<VariantAttributeResponse> response = variantAttributeService.getAttributesForVariant(1L);

        assertEquals(1, response.size());
        assertEquals(100L, response.get(0).id());
        assertEquals(10L, response.get(0).attributeValueId());
        assertEquals("Medium", response.get(0).value());
        assertEquals("Size", response.get(0).attributeTypeName());

        verify(productVariantRepository).findById(1L);
        verify(variantAttributeRepository).findByProductVariant(variant);
    }

    @Test
    void addAttributeToVariant_whenAttributeIsNew_savesVariantAttribute() {
        ProductVariant variant = new ProductVariant();
        variant.setId(1L);

        AttributeType colorType = new AttributeType();
        colorType.setId(2L);
        colorType.setName("Color");

        AttributeValue pink = new AttributeValue();
        pink.setId(20L);
        pink.setValue("Pink");
        pink.setAttributeType(colorType);

        when(productVariantRepository.findById(1L))
                .thenReturn(Optional.of(variant));

        when(attributeValueRepository.findById(20L))
                .thenReturn(Optional.of(pink));

        when(variantAttributeRepository.existsByProductVariantAndAttributeValue(variant, pink))
                .thenReturn(false);

        when(variantAttributeRepository.findByProductVariant(variant))
                .thenReturn(List.of());

        when(variantAttributeRepository.save(any(VariantAttribute.class)))
                .thenAnswer(invocation -> {
                    VariantAttribute variantAttribute = invocation.getArgument(0);
                    variantAttribute.setId(200L);
                    return variantAttribute;
                });

        VariantAttributeResponse response = variantAttributeService.addAttributeToVariant(1L, 20L);

        assertEquals(200L, response.id());
        assertEquals(20L, response.attributeValueId());
        assertEquals("Pink", response.value());
        assertEquals("Color", response.attributeTypeName());

        verify(productVariantRepository).findById(1L);
        verify(attributeValueRepository).findById(20L);
        verify(variantAttributeRepository).save(any(VariantAttribute.class));
    }

    @Test
    void addAttributeToVariant_whenExactAttributeAlreadyExists_throwsIllegalStateException() {
        ProductVariant variant = new ProductVariant();
        variant.setId(1L);

        AttributeType sizeType = new AttributeType();
        sizeType.setId(1L);
        sizeType.setName("Size");

        AttributeValue medium = new AttributeValue();
        medium.setId(10L);
        medium.setValue("Medium");
        medium.setAttributeType(sizeType);

        when(productVariantRepository.findById(1L))
                .thenReturn(Optional.of(variant));

        when(attributeValueRepository.findById(10L))
                .thenReturn(Optional.of(medium));

        when(variantAttributeRepository.existsByProductVariantAndAttributeValue(variant, medium))
                .thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                variantAttributeService.addAttributeToVariant(1L, 10L)
        );

        verify(variantAttributeRepository, never()).save(any(VariantAttribute.class));
    }

    @Test
    void addAttributeToVariant_whenSameAttributeTypeAlreadyExists_throwsIllegalStateException() {
        ProductVariant variant = new ProductVariant();
        variant.setId(1L);

        AttributeType sizeType = new AttributeType();
        sizeType.setId(1L);
        sizeType.setName("Size");

        AttributeValue medium = new AttributeValue();
        medium.setId(10L);
        medium.setValue("Medium");
        medium.setAttributeType(sizeType);

        AttributeValue large = new AttributeValue();
        large.setId(11L);
        large.setValue("Large");
        large.setAttributeType(sizeType);

        VariantAttribute existingSizeAttribute = new VariantAttribute();
        existingSizeAttribute.setId(100L);
        existingSizeAttribute.setProductVariant(variant);
        existingSizeAttribute.setAttributeValue(medium);

        when(productVariantRepository.findById(1L))
                .thenReturn(Optional.of(variant));

        when(attributeValueRepository.findById(11L))
                .thenReturn(Optional.of(large));

        when(variantAttributeRepository.existsByProductVariantAndAttributeValue(variant, large))
                .thenReturn(false);

        when(variantAttributeRepository.findByProductVariant(variant))
                .thenReturn(List.of(existingSizeAttribute));

        assertThrows(IllegalStateException.class, () ->
                variantAttributeService.addAttributeToVariant(1L, 11L)
        );

        verify(variantAttributeRepository, never()).save(any(VariantAttribute.class));
    }

    @Test
    void removeAttributeFromVariant_whenVariantAttributeExists_deletesVariantAttribute() {
        VariantAttribute variantAttribute = new VariantAttribute();
        variantAttribute.setId(100L);

        when(variantAttributeRepository.findById(100L))
                .thenReturn(Optional.of(variantAttribute));

        variantAttributeService.removeAttributeFromVariant(100L);

        verify(variantAttributeRepository).findById(100L);
        verify(variantAttributeRepository).delete(variantAttribute);
    }

    @Test
    void removeAttributeFromVariant_whenVariantAttributeMissing_throwsRuntimeException() {
        when(variantAttributeRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                variantAttributeService.removeAttributeFromVariant(99L)
        );

        verify(variantAttributeRepository).findById(99L);
        verify(variantAttributeRepository, never()).delete(any(VariantAttribute.class));
    }
}