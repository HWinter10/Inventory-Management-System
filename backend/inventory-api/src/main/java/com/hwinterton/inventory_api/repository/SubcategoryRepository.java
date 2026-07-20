package com.hwinterton.inventory_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hwinterton.inventory_api.model.Category;
import com.hwinterton.inventory_api.model.Subcategory;

/**
 * Repository for Subcategory database access
 * 
 * <p>Extends JpaRepository to ingerit common CRUS methods and defines lookup
 * methods for subcategories under specific category</p>
 */
public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {

    List<Subcategory> findByCategory(Category category);

    // prevent duplicate subcategory names
    boolean existsByNameAndCategory(String name, Category category);

    // to help with checking if cat has subcats before deletion
    boolean existsByCategory(Category category);
}