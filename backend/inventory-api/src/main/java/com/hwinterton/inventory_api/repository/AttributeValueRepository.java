package com.hwinterton.inventory_api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hwinterton.inventory_api.model.AttributeType;
import com.hwinterton.inventory_api.model.AttributeValue;

/**
 * Repository for AttributeValue database access.
 *
 * <p>Extends JpaRepository to inherit common CRUD methods and defines
 * lookup methods for values that belong to an attribute type</p>
 */
public interface AttributeValueRepository extends JpaRepository<AttributeValue, Long> {

    List<AttributeValue> findByAttributeType(AttributeType attributeType);

    // prevent duplicates within the same type
    boolean existsByAttributeTypeAndValue(AttributeType attributeType, String value);
}