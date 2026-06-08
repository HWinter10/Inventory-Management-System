package com.hwinterton.inventory_api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hwinterton.inventory_api.dto.category.CategoryRequest;
import com.hwinterton.inventory_api.dto.category.CategoryResponse;
import com.hwinterton.inventory_api.model.Category;
import com.hwinterton.inventory_api.repository.CategoryRepository;
import com.hwinterton.inventory_api.repository.SubcategoryRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class responsible for all Category business logic.
 *
 * <p>Handles creation, retrieval, update, and deletion of categories. Enforces business 
 * rules such as duplicate name prevention and subcategory existence checks before deletion.
 * </p>
 *
 */
@Slf4j // Lombok: logging feature helper, call replaced need for standard dependencies fields for Slf4j logging
@Service
public class CategoryService {
    // dependency fields
    private final SubcategoryRepository subcategoryRepository;
    private final CategoryRepository categoryRepository;    

    // construct inject
    public CategoryService(CategoryRepository categoryRepository, SubcategoryRepository subcategoryRepository){
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
    };

    /**
     * Method: retrieves all categories 
     * 
     * @return list of categories
     */
    public List<CategoryResponse> getAllCategories(){
        log.info("Fetching all categories");

        List<Category> categories = categoryRepository.findAll();
        log.info("Retrieved {} categories", categories.size());
        
        return categories.stream()
            .map(category -> new CategoryResponse(
                category.getId(), 
                category.getName(), 
                category.getDescription()))
            .toList();
    }

    /**
     * Method: retrieves category by Id
     * 
     * @param id of category to retrieve
     * @return transformed DTO of id, name, description
     * @throws RuntimeException
     */
    public CategoryResponse getCategoryById(Long id){
        log.info("Fetching category with id {}", id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Category not found with id: {}", id);
                return new RuntimeException("Category not found");
            });
            return new CategoryResponse(
            category.getId(), 
            category.getName(), 
            category.getDescription());
    }

    /**
     * Method: creates category
     * 
     * @param request  the category data to create
     * @return transformed DTO of id, name, description
     * @throws IllegalArgumentException
     */
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Attempting to create category with name: {}", request.name());

        if (categoryRepository.existsByName(request.name())) {
            log.warn("Duplicate category name attempted: {}", request.name());
            throw new IllegalArgumentException("Category name already exists.");
        }
        // create new category object, set its fields
        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        // save
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", savedCategory.getId());
        // convert to DTO and return
        return new CategoryResponse(
            savedCategory.getId(), 
            savedCategory.getName(), 
            savedCategory.getDescription() 
        );
    }

    /**
     * Method: updates category
     * 
     * @param id of category to retrieve
     * @param request  the category data to update
     * @return transformed DTO of id, name, description
     * @throws RuntimeException
     */
    public CategoryResponse updateCategory(Long id, CategoryRequest request){
        log.info("Attempting to update category with id: {}", id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Category not found with id: {}", id);
                return new RuntimeException("Category not found");
    });

        category.setName(request.name()); // uses request.name instead of getName etc since
        category.setDescription(request.description()); // we are touching a record (CategoryRequest)
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with id: {}", updatedCategory.getId());

        return new CategoryResponse(
            updatedCategory.getId(), // can use standard getters since we are touching a standard
            updatedCategory.getName(), // category entity (updatedCategory) and not a record
            updatedCategory.getDescription()
        );
    }

    /**
     * Method : deletes category
     * 
     * @param id of category to retrieve
     * @throws RuntimeException
     * @throws IllegalStateException
     */
    public void deleteCategory(Long id) {
        log.info("Attempting to delet category with id: {}", id);

        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Category not found with id: {}", id);
                return new RuntimeException("Category not found");
        });

        if (subcategoryRepository.existsByCategory(category)){
            log.warn("Delete attempted on category with existing subcategories, id: {}", id);
            throw new IllegalStateException ("Cannot delete a category that has subcategories");
        }
        
        categoryRepository.delete(category);
        log.info("Category deleted successfully with id: {}", id);
    }
}