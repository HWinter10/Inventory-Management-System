package com.hwinterton.inventory_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hwinterton.inventory_api.dto.subcategory.SubcategoryRequest;
import com.hwinterton.inventory_api.dto.subcategory.SubcategoryResponse;
import com.hwinterton.inventory_api.model.Category;
import com.hwinterton.inventory_api.model.Subcategory;
import com.hwinterton.inventory_api.repository.CategoryRepository;
import com.hwinterton.inventory_api.repository.ProductRepository;
import com.hwinterton.inventory_api.repository.SubcategoryRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for subcategory business logic.
 *
 * <p>Handles subcategory retrieval, creation, updates, and safe deletion while
 * keeping subcategories connected to their parent categories.</p>
 */
@Slf4j // Lombok: logging feature helper, call replaced need for standard dependencies fields for Slf4j logging
@Service
public class SubcategoryService {
    // dependency fields
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final ProductRepository productRepository;

    // injecting
    public SubcategoryService(
        CategoryRepository categoryRepository, 
        SubcategoryRepository subcategoryRepository, 
        ProductRepository productRepository
    ){
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.productRepository = productRepository;
    }

    /**
     * Method: create subcategory
     * 
     * @param requests subcategory data  
     * @return new subcategory to be filed properly under respective category
     * @throws RuntimeException
     * @throws IllegalArgumentException
     */
    public SubcategoryResponse createSubcategory(SubcategoryRequest request) {
        log.info("Attempting to create subcategory with name: {} under category id: {}", request.name(), request.categoryId());
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new RuntimeException("Category not found"));

        if (subcategoryRepository.existsByNameAndCategory(request.name(), category)){
            throw new IllegalArgumentException("Subcategory name already exists under this category");
        }
        // creating new subcategory object
        Subcategory subcategory = new Subcategory();
        subcategory.setName(request.name());
        subcategory.setDescription(request.description());
        subcategory.setCategory(category);
        // saving
        Subcategory savedSubcategory = subcategoryRepository.save(subcategory);
        log.info("Subcategory created successfully with id: {}", savedSubcategory.getId());
        // returning transformed DTO object
        return new SubcategoryResponse(
            savedSubcategory.getId(),
            savedSubcategory.getName(),
            savedSubcategory.getDescription(),
            savedSubcategory.getCategory().getId(),
            savedSubcategory.getCategory().getName()
        );
    }
    /**
     * Method: retrieves all subcategories
     * 
     * @return list of subcategory response DTOs
     */
    public List<SubcategoryResponse> getAllSubcategories(){
        log.info("Fetching all subcategories");
        List<Subcategory> subcategories = subcategoryRepository.findAll();

        return subcategories.stream()
            .map(subcategory -> new SubcategoryResponse(
                subcategory.getId(),
                subcategory.getName(),
                subcategory.getDescription(),
                subcategory.getCategory().getId(),
                subcategory.getCategory().getName()
            ))
            .toList();
    } 

    /**
     * Method: retrieves all subcategories under one category
     *
     * @param categoryId the parent category ID
     * @return list of subcategory response DTOs under the category
     * @throws RuntimeException if the category is not found
     */
    public List<SubcategoryResponse> getSubcategoriesByCategory(Long categoryId) {
        log.info("Fetching subcategories under category id: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        List<Subcategory> subcategories = subcategoryRepository.findByCategory(category);

        return subcategories.stream()
                .map(subcategory -> new SubcategoryResponse(
                        subcategory.getId(),
                        subcategory.getName(),
                        subcategory.getDescription(),
                        subcategory.getCategory().getId(),
                        subcategory.getCategory().getName()
                ))
                .toList();
    }

    /**
     * Method: retrieves subcategory by Id
     *
     * @param id the subcategory ID
     * @return the matching subcategory as a response DTO
     * @throws RuntimeException
     */
    public SubcategoryResponse getSubcategoryById (Long id){
        log.info("Fetching subcategory with id: {}", id);
        Subcategory subcategory = subcategoryRepository.findById(id)    
            .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        return new SubcategoryResponse(
            subcategory.getId(),
            subcategory.getName(),
            subcategory.getDescription(),
            subcategory.getCategory().getId(),
            subcategory.getCategory().getName()
        );
    }

    /**
     * Method: updates an existing subcategory
     *
     * @param id the subcategory ID
     * @param request the updated subcategory data sent from the frontend
     * @return the updated subcategory as a response DTO
     * @throws RuntimeException
     * @throws IllegalArgumentException
     */
    public SubcategoryResponse updateSubcategory(Long id, SubcategoryRequest request) {
        log.info("Attempting to update subcategory with id: {}", id);
        Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (subcategoryRepository.existsByNameAndCategory(request.name(), category)
                && !subcategory.getName().equals(request.name())) {
            log.warn("Duplicate subcategory name attempted during update: {} under category id: {}", request.name(), request.categoryId());
                throw new IllegalArgumentException("Subcategory name already exists under this category");
        }

        subcategory.setName(request.name());
        subcategory.setDescription(request.description());
        subcategory.setCategory(category);

        Subcategory updatedSubcategory = subcategoryRepository.save(subcategory);
        log.info("Subcategory updated successfully with id: {}", updatedSubcategory.getId());
        return new SubcategoryResponse(
                updatedSubcategory.getId(),
                updatedSubcategory.getName(),
                updatedSubcategory.getDescription(),
                updatedSubcategory.getCategory().getId(),
                updatedSubcategory.getCategory().getName()
        );
    }

    /**
     * Method: deletes subcategory if it does not have products attached
     *
     * @param id the subcategory ID
     * @throws RuntimeException
     * @throws IllegalStateException
     */
    public void deleteSubcategory(Long id) {
        log.info("Attempting to delete subcategory with id: {}", id);
        Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        // Subcategories with products attached should not be deleted.
        if (productRepository.existsBySubcategory(subcategory)) {
            log.warn("Delete attempted on subcategory with existing products, id: {}", id);
            throw new IllegalStateException("Cannot delete subcategory that has products attached");
        }

        subcategoryRepository.delete(subcategory);
        log.info("Subcategory deleted successfully with id: {}", id);
    }
}