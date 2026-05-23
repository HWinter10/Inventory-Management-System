package com.hwinterton.inventory_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hwinterton.inventory_api.model.Product;
import com.hwinterton.inventory_api.model.ProductVariant;

/**
 * Repository for ProductVariant database access.
 *
 * <p>Extends JpaRepository to inherit common CRUD methods and defines
 * lookup methods for inventory-level product variant data</p>
 */
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProduct(Product product);

    List<ProductVariant> findByActive(boolean active);

    // low-stock inventory view
    @Query("""
            SELECT variant
            FROM ProductVariant variant
            WHERE variant.active = true
            AND variant.quantityOnHand <= variant.lowStockThreshold
            """)
    List<ProductVariant> findBelowThreshold();
}