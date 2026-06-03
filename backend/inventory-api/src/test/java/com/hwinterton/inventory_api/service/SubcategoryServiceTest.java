package com.hwinterton.inventory_api.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hwinterton.inventory_api.dto.subcategory.SubcategoryRequest;
import com.hwinterton.inventory_api.dto.subcategory.SubcategoryResponse;
import com.hwinterton.inventory_api.model.Category;
import com.hwinterton.inventory_api.model.Subcategory;
import com.hwinterton.inventory_api.repository.CategoryRepository;
import com.hwinterton.inventory_api.repository.ProductRepository;
import com.hwinterton.inventory_api.repository.SubcategoryRepository;
import com.hwinterton.inventory_api.service.SubcategoryService;

@ExtendWith(MockitoExtension.class)
public class SubcategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @Mock
    private ProductRepository productRepository;

    private SubcategoryService subcategoryService;

    @BeforeEach
    void setUp() {
        subcategoryService = new SubcategoryService(categoryRepository, subcategoryRepository, productRepository);
    }

    // testing getAllSubcategories returns list of SubcategoryResponse
    @Test
    void getAllSubcategories_returnsListOfSubcategoryResponse() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Drinks");

        Subcategory subcategory1 = new Subcategory();
        subcategory1.setId(1L);
        subcategory1.setName("Energy Drinks");
        subcategory1.setDescription("All caffeine focused drinks");
        subcategory1.setCategory(category);

        Subcategory subcategory2 = new Subcategory();
        subcategory2.setId(2L);
        subcategory2.setName("Protein Drinks");
        subcategory2.setDescription("All protein focused drinks");
        subcategory2.setCategory(category);

        when(subcategoryRepository.findAll())
            .thenReturn(List.of(subcategory1, subcategory2));

        List<SubcategoryResponse> response = subcategoryService.getAllSubcategories();

        assertEquals(2, response.size());
        assertEquals("Energy Drinks", response.get(0).name());
        assertEquals("Protein Drinks", response.get(1).name());

        verify(subcategoryRepository).findAll();
    }

    // testing getSubcategoryById happy path
    @Test
    void getSubcategoryById_validId_returnsSubcategoryResponse() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Drinks");

        Subcategory subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Energy Drinks");
        subcategory.setDescription("All caffeine focused drinks");
        subcategory.setCategory(category);

        when(subcategoryRepository.findById(1L))
            .thenReturn(Optional.of(subcategory));

        SubcategoryResponse response = subcategoryService.getSubcategoryById(1L);

        assertEquals("Energy Drinks", response.name());
        assertEquals("All caffeine focused drinks", response.description());
        assertEquals(1L, response.categoryId());

        verify(subcategoryRepository).findById(1L);
    }

    // testing getSubcategoryById fail path
    @Test
    void getSubcategoryById_invalidId_throwsRuntimeException() {
        when(subcategoryRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> subcategoryService.getSubcategoryById(99L));

        verify(subcategoryRepository).findById(99L);
    }

    // testing createSubcategory happy path
    @Test
    void createSubcategory_validRequest_returnsSubcategoryResponse() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Drinks");

        SubcategoryRequest request = new SubcategoryRequest("Energy Drinks", "All caffeine focused drinks", 1L);

        Subcategory savedSubcategory = new Subcategory();
        savedSubcategory.setId(1L);
        savedSubcategory.setName("Energy Drinks");
        savedSubcategory.setDescription("All caffeine focused drinks");
        savedSubcategory.setCategory(category);

        when(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category));

        when(subcategoryRepository.existsByNameAndCategory("Energy Drinks", category))
            .thenReturn(false);

        when(subcategoryRepository.save(any(Subcategory.class)))
            .thenReturn(savedSubcategory);

        SubcategoryResponse response = subcategoryService.createSubcategory(request);

        assertEquals("Energy Drinks", response.name());
        assertEquals(1L, response.categoryId());

        verify(categoryRepository).findById(1L);
        verify(subcategoryRepository).existsByNameAndCategory("Energy Drinks", category);
        verify(subcategoryRepository).save(any(Subcategory.class));
    }

    // testing createSubcategory fail path - duplicate name
    @Test
    void createSubcategory_duplicateName_throwsIllegalArgumentException() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Drinks");

        SubcategoryRequest request = new SubcategoryRequest("Energy Drinks", "All caffeine focused drinks", 1L);

        when(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category));

        when(subcategoryRepository.existsByNameAndCategory("Energy Drinks", category))
            .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> subcategoryService.createSubcategory(request));

        verify(subcategoryRepository, never()).save(any());
    }

    // testing updateSubcategory happy path
    @Test
    void updateSubcategory_validRequest_returnsSubcategoryResponse() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Drinks");

        SubcategoryRequest request = new SubcategoryRequest("Updated Energy Drinks", "Updated description", 1L);

        Subcategory existingSubcategory = new Subcategory();
        existingSubcategory.setId(1L);
        existingSubcategory.setName("Energy Drinks");
        existingSubcategory.setDescription("All caffeine focused drinks");
        existingSubcategory.setCategory(category);

        Subcategory updatedSubcategory = new Subcategory();
        updatedSubcategory.setId(1L);
        updatedSubcategory.setName("Updated Energy Drinks");
        updatedSubcategory.setDescription("Updated description");
        updatedSubcategory.setCategory(category);

        when(subcategoryRepository.findById(1L))
            .thenReturn(Optional.of(existingSubcategory));

        when(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category));

        when(subcategoryRepository.existsByNameAndCategory("Updated Energy Drinks", category))
            .thenReturn(false);

        when(subcategoryRepository.save(any(Subcategory.class)))
            .thenReturn(updatedSubcategory);

        SubcategoryResponse response = subcategoryService.updateSubcategory(1L, request);

        assertEquals("Updated Energy Drinks", response.name());
        assertEquals("Updated description", response.description());

        verify(subcategoryRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(subcategoryRepository).save(any(Subcategory.class));
    }

    // testing updateSubcategory fail path - not found
    @Test
    void updateSubcategory_invalidId_throwsRuntimeException() {
        SubcategoryRequest request = new SubcategoryRequest("Updated Energy Drinks", "Updated description", 1L);

        when(subcategoryRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> subcategoryService.updateSubcategory(99L, request));

        verify(subcategoryRepository).findById(99L);
        verify(subcategoryRepository, never()).save(any());
    }

    // testing deleteSubcategory happy path
    @Test
    void deleteSubcategory_validId_noProducts_deletesSubcategory() {
        Subcategory subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Energy Drinks");

        when(subcategoryRepository.findById(1L))
            .thenReturn(Optional.of(subcategory));

        when(productRepository.existsBySubcategory(subcategory))
            .thenReturn(false);

        subcategoryService.deleteSubcategory(1L);

        verify(subcategoryRepository).findById(1L);
        verify(productRepository).existsBySubcategory(subcategory);
        verify(subcategoryRepository).delete(subcategory);
    }

    // testing deleteSubcategory fail path - has products
    @Test
    void deleteSubcategory_hasProducts_throwsIllegalStateException() {
        Subcategory subcategory = new Subcategory();
        subcategory.setId(1L);
        subcategory.setName("Energy Drinks");

        when(subcategoryRepository.findById(1L))
            .thenReturn(Optional.of(subcategory));

        when(productRepository.existsBySubcategory(subcategory))
            .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> subcategoryService.deleteSubcategory(1L));

        verify(subcategoryRepository, never()).delete(any());
    }
}