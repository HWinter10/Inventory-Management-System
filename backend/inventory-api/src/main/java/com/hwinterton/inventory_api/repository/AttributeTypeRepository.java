package com.hwinterton.inventory_api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hwinterton.inventory_api.model.AttributeType;

/**
 * Repository for AttributeType database access.
 *
 * <p>Extends JpaRepository to inherit common CRUD methods and defines
 * lookup methods for attribute type names such as Color, Size, or Flavor</p>
 */
public interface AttributeTypeRepository extends JpaRepository<AttributeType, Long> {

    Optional<AttributeType> findByName(String name);

    // prevent duplicate names attribute type names
    boolean existsByName(String name);
}