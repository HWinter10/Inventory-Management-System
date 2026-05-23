package com.hwinterton.inventory_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hwinterton.inventory_api.model.AttributeValue;
import com.hwinterton.inventory_api.model.ProductVariant;
import com.hwinterton.inventory_api.model.VariantAttribute;

/**
 * Repository for VariantAttribute database access.
 *
 * <p>Extends JpaRepository to inherit common CRUD methods and defines
 * lookup methods for the attributes attached to a product variant</p>
 */
public interface VariantAttributeRepository extends JpaRepository<VariantAttribute, Long> {

    List<VariantAttribute> findByProductVariant(ProductVariant productVariant);

    // prevent the same value from being added twice
    boolean existsByProductVariantAndAttributeValue(
            ProductVariant productVariant,
            AttributeValue attributeValue
    );
}