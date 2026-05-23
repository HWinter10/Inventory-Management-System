package com.hwinterton.inventory_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hwinterton.inventory_api.model.Product;
import com.hwinterton.inventory_api.model.Subcategory;

/**
 * Repository for Product database access.
 *
 * <p>Extends JpaRepository to inherit common CRUD methods and defines
 * lookup methods for product-level filtering.</p>
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findBySubcategory(Subcategory subcategory);

    // separate active products from inactive products
    List<Product> findByActive(boolean active);

    // prevent duplicate product names
    boolean existsByName(String name);
}