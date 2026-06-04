package com.hwinterton.inventory_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hwinterton.inventory_api.model.InventoryAdjustment;
import com.hwinterton.inventory_api.model.ProductVariant;

/**
 * Repository for InventoryAdjustment database access.
 *
 * <p>Extends JpaRepository to inherit common CRUD methods and defines
 * lookup methods for inventory adjustment history.</p>
 */
public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, Long> {

    // retrieve all adjustments for a specific variant
    List<InventoryAdjustment> findByProductVariant(ProductVariant productVariant);

}