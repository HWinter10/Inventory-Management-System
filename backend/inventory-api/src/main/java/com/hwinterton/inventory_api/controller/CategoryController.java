package com.hwinterton.inventory_api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hwinterton.inventory_api.dto.category.CategoryRequest;
import com.hwinterton.inventory_api.dto.category.CategoryResponse;
import com.hwinterton.inventory_api.dto.subcategory.SubcategoryRequest;
import com.hwinterton.inventory_api.dto.subcategory.SubcategoryResponse;
import com.hwinterton.inventory_api.service.CategoryService;
import com.hwinterton.inventory_api.service.SubcategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for category and subcategory endpoints under /api.
 * 
 * <p>Receives category and subcategory requests from the frontend and delegates
 * business logic to CategoryService and SubcategoryService</p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {

    // dependency field used to pass category work to CategoryService
    private final CategoryService categoryService;

    // dependency field used to pass subcategory work to SubcategoryService
    private final SubcategoryService subcategoryService;

    /**
     * Retrieves all categories.
     * 
     * @return list of category response DTOs
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Retrieves one category by ID.
     * 
     * @param id category ID from the URL path
     * @return category response DTO
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    /**
     * Creates a new category.
     * 
     * @param request category data from frontend
     * @return created category response DTO
     */
    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    /**
     * Updates an existing category.
     * 
     * @param id category ID from the URL path
     * @param request updated category data from frontend
     * @return updated category response DTO
     */
    @PatchMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    /**
     * Deletes a category if it is safe to remove.
     * 
     * @param id category ID from the URL path
     * @return no content response when deletion succeeds
     */
    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves all subcategories for one category.
     * 
     * @param id category ID from the URL path
     * @return list of subcategory response DTOs
     */
    @GetMapping("/categories/{id}/subcategories")
    public ResponseEntity<List<SubcategoryResponse>> getSubcategoriesByCategoryId(
            @PathVariable Long id) {

        return ResponseEntity.ok(subcategoryService.getSubcategoriesByCategory(id));
    }

    /**
     * Retrieves one subcategory by ID.
     * 
     * @param id subcategory ID from the URL path
     * @return subcategory response DTO
     */
    @GetMapping("/subcategories/{id}")
    public ResponseEntity<SubcategoryResponse> getSubcategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(subcategoryService.getSubcategoryById(id));
    }

    /**
     * Creates a new subcategory.
     * 
     * @param request subcategory data from frontend
     * @return created subcategory response DTO
     */
    @PostMapping("/subcategories")
    public ResponseEntity<SubcategoryResponse> createSubcategory(
            @Valid @RequestBody SubcategoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subcategoryService.createSubcategory(request));
    }

    /**
     * Updates an existing subcategory.
     * 
     * @param id subcategory ID from the URL path
     * @param request updated subcategory data from frontend
     * @return updated subcategory response DTO
     */
    @PatchMapping("/subcategories/{id}")
    public ResponseEntity<SubcategoryResponse> updateSubcategory(
            @PathVariable Long id,
            @Valid @RequestBody SubcategoryRequest request) {

        return ResponseEntity.ok(subcategoryService.updateSubcategory(id, request));
    }

    /**
     * Deletes a subcategory if it is safe to remove.
     * 
     * @param id subcategory ID from the URL path
     * @return no content response when deletion succeeds
     */
    @DeleteMapping("/subcategories/{id}")
    public ResponseEntity<Void> deleteSubcategory(@PathVariable Long id) {
        subcategoryService.deleteSubcategory(id);
        return ResponseEntity.noContent().build();
    }
}