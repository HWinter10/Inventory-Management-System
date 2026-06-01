package com.hwinterton.inventory_api.service;

import org.hibernate.bytecode.internal.bytebuddy.BytecodeProviderImpl;
import org.springframework.stereotype.Service;
import java.util.List;

import com.hwinterton.inventory_api.dto.category.CategoryResponse;
import com.hwinterton.inventory_api.dto.subcategory.SubcategoryRequest;
import com.hwinterton.inventory_api.dto.subcategory.SubcategoryResponse;
import com.hwinterton.inventory_api.model.Subcategory;
import com.hwinterton.inventory_api.repository.CategoryRepository;
import com.hwinterton.inventory_api.repository.ProductRepository;
import com.hwinterton.inventory_api.repository.SubcategoryRepository;
import com.hwinterton.inventory_api.model.Category;

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
     * Method: retrieves subcategory by Id
     *
     * @param id the subcategory ID
     * @return the matching subcategory as a response DTO
     * @throws RuntimeException
     */
    public SubcategoryResponse getSubcategoryById (Long id){
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
        Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (subcategoryRepository.existsByNameAndCategory(request.name(), category)
                && !subcategory.getName().equals(request.name())) {
            throw new IllegalArgumentException("Subcategory name already exists under this category");
        }

        subcategory.setName(request.name());
        subcategory.setDescription(request.description());
        subcategory.setCategory(category);

        Subcategory updatedSubcategory = subcategoryRepository.save(subcategory);

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
        Subcategory subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subcategory not found"));

        // Subcategories with products attached should not be deleted.
        if (productRepository.existsBySubcategory(subcategory)) {
            throw new IllegalStateException("Cannot delete subcategory that has products attached");
        }

        subcategoryRepository.delete(subcategory);
    }
}