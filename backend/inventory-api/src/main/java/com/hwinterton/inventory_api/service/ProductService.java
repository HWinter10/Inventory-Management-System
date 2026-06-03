package com.hwinterton.inventory_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hwinterton.inventory_api.dto.product.ProductRequest;
import com.hwinterton.inventory_api.dto.product.ProductResponse;
import com.hwinterton.inventory_api.model.Product;
import com.hwinterton.inventory_api.model.Subcategory;
import com.hwinterton.inventory_api.repository.ProductRepository;
import com.hwinterton.inventory_api.repository.SubcategoryRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final SubcategoryRepository subcategoryRepository;

    public ProductService(ProductRepository productRepository, SubcategoryRepository subcategoryRepository) {
        this.productRepository = productRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    /**
     * Retrieves all active products.
     *
     * @return list of active products as response DTOs
     */
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findByActive(true);

        return products.stream()
            .map(product -> new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.isActive(),
                product.getSubcategory().getId(),
                product.getSubcategory().getName()
            ))
            .toList();
    }

    /**
     * Retrieves a product by id.
     *
     * @param id the product ID
     * @return the matching product as a response DTO
     * @throws RuntimeException if product is not found
     */
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.isActive(),
            product.getSubcategory().getId(),
            product.getSubcategory().getName()
        );
    }

    /**
     * Creates a new product under an existing subcategory.
     *
     * @param request the product data from the frontend
     * @return the created product as a response DTO
     * @throws RuntimeException if subcategory is not found
     * @throws IllegalArgumentException if product name already exists
     */
    public ProductResponse createProduct(ProductRequest request) {
        Subcategory subcategory = subcategoryRepository.findById(request.subcategoryId())
            .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        if (productRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Product name already exists");
        }

        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setSubcategory(subcategory);
        product.setActive(true);

        Product savedProduct = productRepository.save(product);

        return new ProductResponse(
            savedProduct.getId(),
            savedProduct.getName(),
            savedProduct.getDescription(),
            savedProduct.isActive(),
            savedProduct.getSubcategory().getId(),
            savedProduct.getSubcategory().getName()
        );
    }

    /**
     * Updates an existing product.
     *
     * @param id the product ID
     * @param request the updated product data
     * @return the updated product as a response DTO
     * @throws RuntimeException if product or subcategory is not found
     * @throws IllegalArgumentException if product name already exists under a different product
     */
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        Subcategory subcategory = subcategoryRepository.findById(request.subcategoryId())
            .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        if (productRepository.existsByName(request.name())
                && !product.getName().equals(request.name())) {
            throw new IllegalArgumentException("Product name already exists");
        }

        product.setName(request.name());
        product.setDescription(request.description());
        product.setSubcategory(subcategory);

        Product updatedProduct = productRepository.save(product);

        return new ProductResponse(
            updatedProduct.getId(),
            updatedProduct.getName(),
            updatedProduct.getDescription(),
            updatedProduct.isActive(),
            updatedProduct.getSubcategory().getId(),
            updatedProduct.getSubcategory().getName()
        );
    }

    /**
     * Soft deletes a product by setting active to false.
     *
     * @param id the product ID
     * @throws RuntimeException if product is not found
     */
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setActive(false);
        productRepository.save(product);
    }
}