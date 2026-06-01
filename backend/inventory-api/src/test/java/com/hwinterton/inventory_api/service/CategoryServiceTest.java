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

import com.hwinterton.inventory_api.dto.category.CategoryRequest;
import com.hwinterton.inventory_api.dto.category.CategoryResponse;
import com.hwinterton.inventory_api.model.Category;
import com.hwinterton.inventory_api.repository.CategoryRepository;
import com.hwinterton.inventory_api.repository.SubcategoryRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, subcategoryRepository);
    }

    // testing getAllCategories returns list of CategoryResponse
    @Test
    void getAllCategories_returnsListOfCategoryResponse() {
        Category category1 = new Category();
        category1.setId(1L);
        category1.setName("Supplements");
        category1.setDescription("All supplements");

        Category category2 = new Category();
        category2.setId(2L);
        category2.setName("Equipment");
        category2.setDescription("All equipment");

        when(categoryRepository.findAll())
            .thenReturn(List.of(category1, category2));

        List<CategoryResponse> response = categoryService.getAllCategories();

        assertEquals(2, response.size());
        assertEquals("Supplements", response.get(0).name());
        assertEquals("Equipment", response.get(1).name());

        verify(categoryRepository).findAll();
    }

    // testing getCategoryById happy path
    @Test
    void getCategoryById_validId_returnsCategoryResponse() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Supplements");
        category.setDescription("All supplements");

        when(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category));

        CategoryResponse response = categoryService.getCategoryById(1L);

        assertEquals("Supplements", response.name());
        assertEquals("All supplements", response.description());

        verify(categoryRepository).findById(1L);
    }

    // testing getCategoryById fail path
    @Test
    void getCategoryById_invalidId_throwsRuntimeException() {
        when(categoryRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoryService.getCategoryById(99L));

        verify(categoryRepository).findById(99L);
    }

    // testing createCategory happy path
    @Test
    void createCategory_validRequest_returnsCategoryResponse() {
        CategoryRequest request = new CategoryRequest("Supplements", "All supplements");

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Supplements");
        savedCategory.setDescription("All supplements");

        when(categoryRepository.existsByName("Supplements"))
            .thenReturn(false);

        when(categoryRepository.save(any(Category.class)))
            .thenReturn(savedCategory);

        CategoryResponse response = categoryService.createCategory(request);

        assertEquals("Supplements", response.name());
        assertEquals("All supplements", response.description());

        verify(categoryRepository).existsByName("Supplements");
        verify(categoryRepository).save(any(Category.class));
    }

    // testing createCategory fail path - duplicate name
    @Test
    void createCategory_duplicateName_throwsIllegalArgumentException() {
        CategoryRequest request = new CategoryRequest("Supplements", "All supplements");

        when(categoryRepository.existsByName("Supplements"))
            .thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(request));

        verify(categoryRepository).existsByName("Supplements");
        verify(categoryRepository, never()).save(any());
    }

    // testing updateCategory happy path
    @Test
    void updateCategory_validRequest_returnsCategoryResponse() {
        CategoryRequest request = new CategoryRequest("Updated Supplements", "Updated description");

        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setName("Supplements");
        existingCategory.setDescription("All supplements");

        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Updated Supplements");
        updatedCategory.setDescription("Updated description");

        when(categoryRepository.findById(1L))
            .thenReturn(Optional.of(existingCategory));

        when(categoryRepository.save(any(Category.class)))
            .thenReturn(updatedCategory);

        CategoryResponse response = categoryService.updateCategory(1L, request);

        assertEquals("Updated Supplements", response.name());
        assertEquals("Updated description", response.description());

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    // testing updateCategory fail path - category not found
    @Test
    void updateCategory_invalidId_throwsRuntimeException() {
        CategoryRequest request = new CategoryRequest("Updated Supplements", "Updated description");

        when(categoryRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> categoryService.updateCategory(99L, request));

        verify(categoryRepository).findById(99L);
        verify(categoryRepository, never()).save(any());
    }

    // testing deleteCategory happy path
    @Test
    void deleteCategory_validId_noSubcategories_deletesCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Supplements");
        category.setDescription("All supplements");

        when(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category));

        when(subcategoryRepository.existsByCategory(category))
            .thenReturn(false);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).findById(1L);
        verify(subcategoryRepository).existsByCategory(category);
        verify(categoryRepository).delete(category);
    }

    // testing deleteCategory fail path - has subcategories
    @Test
    void deleteCategory_hasSubcategories_throwsIllegalStateException() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Supplements");
        category.setDescription("All supplements");

        when(categoryRepository.findById(1L))
            .thenReturn(Optional.of(category));

        when(subcategoryRepository.existsByCategory(category))
            .thenReturn(true);

        assertThrows(IllegalStateException.class, () -> categoryService.deleteCategory(1L));

        verify(categoryRepository).findById(1L);
        verify(subcategoryRepository).existsByCategory(category);
        verify(categoryRepository, never()).delete(any());
    }
}
