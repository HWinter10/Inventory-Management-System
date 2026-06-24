package com.hwinterton.inventory_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hwinterton.inventory_api.dto.product.ProductRequest;
import com.hwinterton.inventory_api.dto.product.ProductResponse;
import com.hwinterton.inventory_api.dto.variant.ProductVariantRequest;
import com.hwinterton.inventory_api.dto.variant.ProductVariantResponse;
import com.hwinterton.inventory_api.service.ProductService;
import com.hwinterton.inventory_api.service.ProductVariantService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for product and variant endpoints under /api
 * 
 * <p>Receives product and variant requests from the frontend and delegates
 * business logic to ProductService and ProductVariantService</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    // dependency field used to pass product work to ProductService
    private final ProductService productService;

    // dependency field used to pass product variant work to ProductVariantService
    private final ProductVariantService productVariantService;

    /**
     * Retrieves all active products.
     * 
     * @return list of product response DTOs
     */
    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    /**
     * Retrieves one product by ID.
     * 
     * @param id product ID from the URL path
     * @return product response DTO
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * Creates a new product.
     * 
     * @param request product data from frontend
     * @return created product response DTO
     */
    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    /**
     * Updates an existing product.
     * 
     * @param id product ID from the URL path
     * @param request updated product data from frontend
     * @return updated product response DTO
     */
    @PatchMapping("/products/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * Soft deletes a product.
     * 
     * @param id product ID from the URL path
     * @return no content response when soft deletion succeeds
     */
    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all variants for one product.
     * 
     * @param id product ID from the URL path
     * @return list of product variant response DTOs
     */
    @GetMapping("/products/{id}/variants")
    public ResponseEntity<List<ProductVariantResponse>> getVariantsByProductId(
            @PathVariable Long id) {

        return ResponseEntity.ok(productVariantService.getVariantsByProduct(id));
    }

    /**
     * Retrieves all low-stock product variants.
     * 
     * @return list of low-stock product variant response DTOs
     */
    @GetMapping("/variants/low-stock")
    public ResponseEntity<List<ProductVariantResponse>> getLowStockVariants() {
        return ResponseEntity.ok(productVariantService.getLowStockVariants());
    }

    /**
     * Retrieves all active product variants.
     * 
     * @return list of product variant response DTOs
     */
    @GetMapping("/variants")
    public ResponseEntity<List<ProductVariantResponse>> getAllVariants() {
        return ResponseEntity.ok(productVariantService.getAllVariants());
    }

    /**
     * Retrieves one product variant by ID.
     * 
     * @param id product variant ID from the URL path
     * @return product variant response DTO
     */
    @GetMapping("/variants/{id}")
    public ResponseEntity<ProductVariantResponse> getVariantById(@PathVariable Long id) {
        return ResponseEntity.ok(productVariantService.getVariantById(id));
    }

    /**
     * Creates a new product variant.
     * 
     * @param request product variant data from frontend
     * @return created product variant response DTO
     */
    @PostMapping("/variants")
    public ResponseEntity<ProductVariantResponse> createVariant(
            @Valid @RequestBody ProductVariantRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productVariantService.createVariant(request));
    }

    /**
     * Updates an existing product variant.
     * 
     * @param id product variant ID from the URL path
     * @param request updated product variant data from frontend
     * @return updated product variant response DTO
     */
    @PatchMapping("/variants/{id}")
    public ResponseEntity<ProductVariantResponse> updateVariant(
            @PathVariable Long id,
            @Valid @RequestBody ProductVariantRequest request) {

        return ResponseEntity.ok(productVariantService.updateVariant(id, request));
    }

    /**
     * Soft deletes a product variant.
     * 
     * @param id product variant ID from the URL path
     * @return no content response when soft deletion succeeds
     */
    @DeleteMapping("/variants/{id}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Long id) {
        productVariantService.deleteVariant(id);
        return ResponseEntity.noContent().build();
    }
}