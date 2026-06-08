package com.hwinterton.inventory_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hwinterton.inventory_api.dto.attribute.VariantAttributeResponse;
import com.hwinterton.inventory_api.dto.variant.ProductVariantRequest;
import com.hwinterton.inventory_api.dto.variant.ProductVariantResponse;
import com.hwinterton.inventory_api.model.Product;
import com.hwinterton.inventory_api.model.ProductVariant;
import com.hwinterton.inventory_api.repository.ProductRepository;
import com.hwinterton.inventory_api.repository.ProductVariantRepository;
import com.hwinterton.inventory_api.repository.VariantAttributeRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for product variant business logic.
 *
 * <p>Handles variant retrieval, creation, updates, soft deletion, low stock
 * lookups, and conversion of attached variant attributes into response DTOs.</p>
 */
@Slf4j // Lombok: logging feature helper, call replaced need for standard dependencies fields for Slf4j logging
@Service
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final VariantAttributeRepository variantAttributeRepository;

    public ProductVariantService(
        ProductVariantRepository productVariantRepository,
        ProductRepository productRepository,
        VariantAttributeRepository variantAttributeRepository
    ) {
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
        this.variantAttributeRepository = variantAttributeRepository;
    }

    /**
     * Converts a ProductVariant entity to a ProductVariantResponse DTO.
     * Fetches associated attributes and maps them to VariantAttributeResponse.
     *
     * @param variant the ProductVariant entity to convert
     * @return the converted ProductVariantResponse DTO
     */
    private ProductVariantResponse toResponse(ProductVariant variant) {
        List<VariantAttributeResponse> attributes = variantAttributeRepository
            .findByProductVariant(variant)
            .stream()
            .map(va -> new VariantAttributeResponse(
                va.getId(),
                va.getAttributeValue().getId(),
                va.getAttributeValue().getValue(),
                va.getAttributeValue().getAttributeType().getName()
            ))
            .toList();

        return new ProductVariantResponse(
            variant.getId(),
            variant.getProduct().getId(),
            variant.getProduct().getName(),
            variant.getSku(),
            variant.getDisplayName(),
            variant.getQuantityOnHand(),
            variant.getLowStockThreshold(),
            variant.isActive(),
            attributes
        );
    }

    /**
     * Retrieves all active product variants.
     *
     * @return list of active product variants as response DTOs
     */
    public List<ProductVariantResponse> getAllVariants() {
        log.info("Fetching all active product variants");
        return productVariantRepository.findByActive(true)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Retrieves a product variant by id.
     *
     * @param id the variant ID
     * @return the matching variant as a response DTO
     * @throws RuntimeException if variant is not found
     */
    public ProductVariantResponse getVariantById(Long id) {
        log.info("Fetching product variant with id: {}", id);
        ProductVariant variant = productVariantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product variant not found"));

        return toResponse(variant);
    }

    /**
     * Retrieves all variants belonging to a specific product.
     *
     * @param productId the product ID
     * @return list of variants as response DTOs
     * @throws RuntimeException if product is not found
     */
    public List<ProductVariantResponse> getVariantsByProduct(Long productId) {
        log.info("Fetching variants for product id: {}", productId);
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        return productVariantRepository.findByProduct(product)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    /**
     * Creates a new product variant.
     *
     * @param request the variant data from the frontend
     * @return the created variant as a response DTO
     * @throws RuntimeException if product is not found
     */
    public ProductVariantResponse createVariant(ProductVariantRequest request) {
        log.info("Attempting to create variant with SKU: {} for product id: {}", request.sku(), request.productId());
        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(request.sku());
        variant.setDisplayName(request.displayName());
        variant.setQuantityOnHand(request.quantityOnHand());
        variant.setLowStockThreshold(request.lowStockThreshold());
        variant.setActive(true);

        ProductVariant savedVariant = productVariantRepository.save(variant);
        log.info("Product variant created successfully with id: {}", savedVariant.getId());
        return toResponse(savedVariant);
    }

    /**
     * Updates an existing product variant.
     *
     * @param id the variant ID
     * @param request the updated variant data
     * @return the updated variant as a response DTO
     * @throws RuntimeException if variant or product is not found
     */
    public ProductVariantResponse updateVariant(Long id, ProductVariantRequest request) {
        log.info("Attempting to update product variant with id: {}", id);
        ProductVariant variant = productVariantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product variant not found"));

        Product product = productRepository.findById(request.productId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

        variant.setProduct(product);
        variant.setSku(request.sku());
        variant.setDisplayName(request.displayName());
        variant.setQuantityOnHand(request.quantityOnHand());
        variant.setLowStockThreshold(request.lowStockThreshold());

        ProductVariant updatedVariant = productVariantRepository.save(variant);
        log.info("Product variant updated successfully with id: {}", updatedVariant.getId());
        return toResponse(updatedVariant);
    }

    /**
     * Soft deletes a variant by setting active to false.
     *
     * @param id the variant ID
     * @throws RuntimeException if variant is not found
     */
    public void deleteVariant(Long id) {
        log.info("Attempting to soft delete product variant with id: {}", id);
        ProductVariant variant = productVariantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product variant not found"));
        variant.setActive(false);
        productVariantRepository.save(variant);
        log.info("Product variant soft deleted successfully with id: {}", id);
    }

    /**
     * Retrieves all variants below their low stock threshold.
     *
     * @return list of low stock variants as response DTOs
     */
    public List<ProductVariantResponse> getLowStockVariants() {
        log.info("Fetching all low stock variants");
        return productVariantRepository.findBelowThreshold()
            .stream()
            .map(this::toResponse)
            .toList();
    }
}