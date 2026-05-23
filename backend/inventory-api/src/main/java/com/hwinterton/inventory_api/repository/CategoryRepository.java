package com.hwinterton.inventory_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hwinterton.inventory_api.model.Category;

/**
 * Repository for Category database access
 * 
 * <p>Extends JpaRepository to inherit CRUD methods and defines cat-specific
 * lookup methods needed for inventory mangement</p>
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    // prevent duplicate category names
    boolean existsByName(String name);    
}