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

import com.hwinterton.inventory_api.dto.product.ProductRequest;
import com.hwinterton.inventory_api.dto.product.ProductResponse;
import com.hwinterton.inventory_api.model.Product;
import com.hwinterton.inventory_api.model.Subcategory;
import com.hwinterton.inventory_api.repository.ProductRepository;
import com.hwinterton.inventory_api.repository.SubcategoryRepository;
import com.hwinterton.inventory_api.service.ProductService;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, subcategoryRepository);
    }

    // testing getAllProducts returns only active products
    @Test
    void getAllProducts_returnsActiveProducts() {
        Subcategory subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Energy Drinks");

        Product product1 = new Product();
        product1.setId(1L);
        product1.setName("Protein Bar");
        product1.setDescription("High protein bar");
        product1.setActive(true);
        product1.setSubcategory(subcategory);

        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Creatine");
        product2.setDescription("Pure creatine");
        product2.setActive(true);
        product2.setSubcategory(subcategory);

        when(productRepository.findByActive(true))
            .thenReturn(List.of(product1, product2));

        List<ProductResponse> response = productService.getAllProducts();

        assertEquals(2, response.size());
        assertEquals("Protein Bar", response.get(0).name());
        assertEquals("Creatine", response.get(1).name());

        verify(productRepository).findByActive(true);
    }

    // testing getProductById happy path
    @Test
    void getProductById_validId_returnsProductResponse() {
        Subcategory subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Energy Drinks");

        Product product = new Product();
        product.setId(1L);
        product.setName("Protein Bar");
        product.setDescription("High protein bar");
        product.setActive(true);
        product.setSubcategory(subcategory);

        when(productRepository.findById(1L))
            .thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertEquals("Protein Bar", response.name());
        assertEquals(true, response.active());
        assertEquals(1L, response.subcategoryId());

        verify(productRepository).findById(1L);
    }

    // testing getProductById fail path
    @Test
    void getProductById_invalidId_throwsRuntimeException() {
        when(productRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductById(99L));

        verify(productRepository).findById(99L);
    }

    // testing createProduct happy path
    @Test
    void createProduct_validRequest_returnsProductResponse() {
        Subcategory subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Energy Drinks");

        ProductRequest request = new ProductRequest("Protein Bar", "High protein bar", 1L);

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Protein Bar");
        savedProduct.setDescription("High protein bar");
        savedProduct.setActive(true);
        savedProduct.setSubcategory(subcategory);

        when(subcategoryRepository.findById(1L))
            .thenReturn(Optional.of(subcategory));

        when(productRepository.existsByName("Protein Bar"))
            .thenReturn(false);

        when(productRepository.save(any(Product.class)))
            .thenReturn(savedProduct);

        ProductResponse response = productService.createProduct(request);

        assertEquals("Protein Bar", response.name());
        assertEquals(true, response.active());
        assertEquals(1L, response.subcategoryId());

        verify(subcategoryRepository).findById(1L);
        verify(productRepository).existsByName("Protein Bar");
        verify(productRepository).save(any(Product.class));
    }

    // testing createProduct fail path - duplicate name
    @Test
    void createProduct_duplicateName_throwsIllegalArgumentException() {
        ProductRequest request = new ProductRequest("Protein Bar", "High protein bar", 1L);

        Subcategory subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Energy Drinks");

        when(subcategoryRepository.findById(1L))
            .thenReturn(Optional.of(subcategory));

        when(productRepository.existsByName("Protein Bar"))
            .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> productService.createProduct(request));

        verify(productRepository, never()).save(any());
    }

    // testing updateProduct happy path
    @Test
    void updateProduct_validRequest_returnsProductResponse() {
        Subcategory subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Energy Drinks");

        ProductRequest request = new ProductRequest("Updated Protein Bar", "Updated description", 1L);

        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setName("Protein Bar");
        existingProduct.setDescription("High protein bar");
        existingProduct.setActive(true);
        existingProduct.setSubcategory(subcategory);

        Product updatedProduct = new Product();
        updatedProduct.setId(1L);
        updatedProduct.setName("Updated Protein Bar");
        updatedProduct.setDescription("Updated description");
        updatedProduct.setActive(true);
        updatedProduct.setSubcategory(subcategory);

        when(productRepository.findById(1L))
            .thenReturn(Optional.of(existingProduct));

        when(subcategoryRepository.findById(1L))
            .thenReturn(Optional.of(subcategory));

        when(productRepository.existsByName("Updated Protein Bar"))
            .thenReturn(false);

        when(productRepository.save(any(Product.class)))
            .thenReturn(updatedProduct);

        ProductResponse response = productService.updateProduct(1L, request);

        assertEquals("Updated Protein Bar", response.name());
        assertEquals("Updated description", response.description());

        verify(productRepository).findById(1L);
        verify(subcategoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    // testing updateProduct fail path - not found
    @Test
    void updateProduct_invalidId_throwsRuntimeException() {
        ProductRequest request = new ProductRequest("Updated Protein Bar", "Updated description", 1L);

        when(productRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.updateProduct(99L, request));

        verify(productRepository).findById(99L);
        verify(productRepository, never()).save(any());
    }

    // testing deleteProduct happy path - soft delete
    @Test
    void deleteProduct_validId_setsActiveFalse() {
        Subcategory subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Energy Drinks");

        Product product = new Product();
        product.setId(1L);
        product.setName("Protein Bar");
        product.setActive(true);
        product.setSubcategory(subcategory);

        when(productRepository.findById(1L))
            .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
            .thenReturn(product);

        productService.deleteProduct(1L);

        assertEquals(false, product.isActive());

        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }

    // testing deleteProduct fail path - not found
    @Test
    void deleteProduct_invalidId_throwsRuntimeException() {
        when(productRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.deleteProduct(99L));

        verify(productRepository).findById(99L);
        verify(productRepository, never()).save(any());
    }
}