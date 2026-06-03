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

import com.hwinterton.inventory_api.dto.variant.ProductVariantRequest;
import com.hwinterton.inventory_api.dto.variant.ProductVariantResponse;
import com.hwinterton.inventory_api.model.Product;
import com.hwinterton.inventory_api.model.ProductVariant;
import com.hwinterton.inventory_api.repository.ProductRepository;
import com.hwinterton.inventory_api.repository.ProductVariantRepository;
import com.hwinterton.inventory_api.repository.VariantAttributeRepository;
import com.hwinterton.inventory_api.service.ProductVariantService;

@ExtendWith(MockitoExtension.class)
public class ProductVariantServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private VariantAttributeRepository variantAttributeRepository;

    private ProductVariantService productVariantService;

    @BeforeEach
    void setUp() {
        productVariantService = new ProductVariantService(
            productVariantRepository,
            productRepository,
            variantAttributeRepository
        );
    }

    // helper to build a base product
    private Product buildProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Protein Bar");
        product.setActive(true);
        return product;
    }

    // helper to build a base variant
    private ProductVariant buildVariant(Product product) {
        ProductVariant variant = new ProductVariant();
        variant.setId(1L);
        variant.setSku("SKU-001");
        variant.setDisplayName("Chocolate Protein Bar");
        variant.setQuantityOnHand(50);
        variant.setLowStockThreshold(10);
        variant.setActive(true);
        variant.setProduct(product);
        return variant;
    }

    // testing getAllVariants returns active variants
    @Test
    void getAllVariants_returnsActiveVariants() {
        Product product = buildProduct();
        ProductVariant variant = buildVariant(product);

        when(productVariantRepository.findByActive(true))
            .thenReturn(List.of(variant));

        when(variantAttributeRepository.findByProductVariant(variant))
            .thenReturn(List.of());

        List<ProductVariantResponse> response = productVariantService.getAllVariants();

        assertEquals(1, response.size());
        assertEquals("SKU-001", response.get(0).sku());

        verify(productVariantRepository).findByActive(true);
    }

    // testing getVariantById happy path
    @Test
    void getVariantById_validId_returnsProductVariantResponse() {
        Product product = buildProduct();
        ProductVariant variant = buildVariant(product);

        when(productVariantRepository.findById(1L))
            .thenReturn(Optional.of(variant));

        when(variantAttributeRepository.findByProductVariant(variant))
            .thenReturn(List.of());

        ProductVariantResponse response = productVariantService.getVariantById(1L);

        assertEquals("SKU-001", response.sku());
        assertEquals("Chocolate Protein Bar", response.displayName());

        verify(productVariantRepository).findById(1L);
    }

    // testing getVariantById fail path
    @Test
    void getVariantById_invalidId_throwsRuntimeException() {
        when(productVariantRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productVariantService.getVariantById(99L));

        verify(productVariantRepository).findById(99L);
    }

    // testing getVariantsByProduct happy path
    @Test
    void getVariantsByProduct_validProductId_returnsVariants() {
        Product product = buildProduct();
        ProductVariant variant = buildVariant(product);

        when(productRepository.findById(1L))
            .thenReturn(Optional.of(product));

        when(productVariantRepository.findByProduct(product))
            .thenReturn(List.of(variant));

        when(variantAttributeRepository.findByProductVariant(variant))
            .thenReturn(List.of());

        List<ProductVariantResponse> response = productVariantService.getVariantsByProduct(1L);

        assertEquals(1, response.size());
        assertEquals("SKU-001", response.get(0).sku());

        verify(productRepository).findById(1L);
        verify(productVariantRepository).findByProduct(product);
    }

    // testing getVariantsByProduct fail path
    @Test
    void getVariantsByProduct_invalidProductId_throwsRuntimeException() {
        when(productRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productVariantService.getVariantsByProduct(99L));

        verify(productRepository).findById(99L);
    }

    // testing createVariant happy path
    @Test
    void createVariant_validRequest_returnsProductVariantResponse() {
        Product product = buildProduct();
        ProductVariant savedVariant = buildVariant(product);

        ProductVariantRequest request = new ProductVariantRequest(1L, "SKU-001", "Chocolate Protein Bar", 50, 10);

        when(productRepository.findById(1L))
            .thenReturn(Optional.of(product));

        when(productVariantRepository.save(any(ProductVariant.class)))
            .thenReturn(savedVariant);

        when(variantAttributeRepository.findByProductVariant(savedVariant))
            .thenReturn(List.of());

        ProductVariantResponse response = productVariantService.createVariant(request);

        assertEquals("SKU-001", response.sku());
        assertEquals("Chocolate Protein Bar", response.displayName());
        assertEquals(true, response.active());

        verify(productRepository).findById(1L);
        verify(productVariantRepository).save(any(ProductVariant.class));
    }

    // testing createVariant fail path - product not found
    @Test
    void createVariant_invalidProductId_throwsRuntimeException() {
        ProductVariantRequest request = new ProductVariantRequest(99L, "SKU-001", "Chocolate Protein Bar", 50, 10);

        when(productRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productVariantService.createVariant(request));

        verify(productRepository).findById(99L);
        verify(productVariantRepository, never()).save(any());
    }

    // testing updateVariant happy path
    @Test
    void updateVariant_validRequest_returnsProductVariantResponse() {
        Product product = buildProduct();
        ProductVariant existingVariant = buildVariant(product);

        ProductVariantRequest request = new ProductVariantRequest(1L, "SKU-002", "Vanilla Protein Bar", 30, 5);

        ProductVariant updatedVariant = new ProductVariant();
        updatedVariant.setId(1L);
        updatedVariant.setSku("SKU-002");
        updatedVariant.setDisplayName("Vanilla Protein Bar");
        updatedVariant.setQuantityOnHand(30);
        updatedVariant.setLowStockThreshold(5);
        updatedVariant.setActive(true);
        updatedVariant.setProduct(product);

        when(productVariantRepository.findById(1L))
            .thenReturn(Optional.of(existingVariant));

        when(productRepository.findById(1L))
            .thenReturn(Optional.of(product));

        when(productVariantRepository.save(any(ProductVariant.class)))
            .thenReturn(updatedVariant);

        when(variantAttributeRepository.findByProductVariant(updatedVariant))
            .thenReturn(List.of());

        ProductVariantResponse response = productVariantService.updateVariant(1L, request);

        assertEquals("SKU-002", response.sku());
        assertEquals("Vanilla Protein Bar", response.displayName());

        verify(productVariantRepository).findById(1L);
        verify(productRepository).findById(1L);
        verify(productVariantRepository).save(any(ProductVariant.class));
    }

    // testing updateVariant fail path - not found
    @Test
    void updateVariant_invalidId_throwsRuntimeException() {
        ProductVariantRequest request = new ProductVariantRequest(1L, "SKU-002", "Vanilla Protein Bar", 30, 5);

        when(productVariantRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productVariantService.updateVariant(99L, request));

        verify(productVariantRepository).findById(99L);
        verify(productVariantRepository, never()).save(any());
    }

    // testing deleteVariant happy path - soft delete
    @Test
    void deleteVariant_validId_setsActiveFalse() {
        Product product = buildProduct();
        ProductVariant variant = buildVariant(product);

        when(productVariantRepository.findById(1L))
            .thenReturn(Optional.of(variant));

        when(productVariantRepository.save(any(ProductVariant.class)))
            .thenReturn(variant);

        productVariantService.deleteVariant(1L);

        assertEquals(false, variant.isActive());

        verify(productVariantRepository).findById(1L);
        verify(productVariantRepository).save(variant);
    }

    // testing deleteVariant fail path - not found
    @Test
    void deleteVariant_invalidId_throwsRuntimeException() {
        when(productVariantRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productVariantService.deleteVariant(99L));

        verify(productVariantRepository).findById(99L);
        verify(productVariantRepository, never()).save(any());
    }

    // testing getLowStockVariants returns variants below threshold
    @Test
    void getLowStockVariants_returnsVariantsBelowThreshold() {
        Product product = buildProduct();
        ProductVariant variant = buildVariant(product);
        variant.setQuantityOnHand(5);

        when(productVariantRepository.findBelowThreshold())
            .thenReturn(List.of(variant));

        when(variantAttributeRepository.findByProductVariant(variant))
            .thenReturn(List.of());

        List<ProductVariantResponse> response = productVariantService.getLowStockVariants();

        assertEquals(1, response.size());
        assertEquals("SKU-001", response.get(0).sku());

        verify(productVariantRepository).findBelowThreshold();
    }
}