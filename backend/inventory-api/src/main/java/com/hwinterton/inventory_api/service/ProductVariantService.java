package com.hwinterton.inventory_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hwinterton.inventory_api.dto.attribute.VariantAttributeResponse;
import com.hwinterton.inventory_api.dto.variant.ProductVariantRequest;
import com.hwinterton.inventory_api.dto.variant.ProductVariantResponse;
import com.hwinterton.inventory_api.model.Product;
import com.hwinterton.inventory_api.model.ProductVariant;
import com.hwinterton.inventory_api.model.VariantAttribute;
import com.hwinterton.inventory_api.repository.ProductRepository;
import com.hwinterton.inventory_api.repository.ProductVariantRepository;
import com.hwinterton.inventory_api.repository.VariantAttributeRepository;

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

        return toResponse(updatedVariant);
    }

    /**
     * Soft deletes a variant by setting active to false.
     *
     * @param id the variant ID
     * @throws RuntimeException if variant is not found
     */
    public void deleteVariant(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product variant not found"));

        variant.setActive(false);
        productVariantRepository.save(variant);
    }

    /**
     * Retrieves all variants below their low stock threshold.
     *
     * @return list of low stock variants as response DTOs
     */
    public List<ProductVariantResponse> getLowStockVariants() {
        return productVariantRepository.findBelowThreshold()
            .stream()
            .map(this::toResponse)
            .toList();
    }
}