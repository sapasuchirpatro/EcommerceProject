package com.ecommerce.project.service;

import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;

public interface CategoryService {
    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryDTO createCategory(CategoryDTO category);
    CategoryDTO deleteCategory(Long categoryId);

    CategoryDTO updateCategory(CategoryDTO category, Long categoryId);

    // This is a default method which is not mandatory to implemented in the implementation class. This method can be accessed using the instance of the implemented class
//    default CategoryDTO updateCategoryDefault(CategoryDTO category, Long categoryId) {
//        return new CategoryDTO();
//    };
}
