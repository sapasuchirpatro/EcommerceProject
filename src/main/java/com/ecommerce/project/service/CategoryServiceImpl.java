package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
//    private final List<Category> categories = new ArrayList<>();
//    private Long categoryID = 1L;
//
//    @Override
//    public List<Category> getAllCategories() {
//        return categories;
//    }
//
//    @Override
//    public void createCategory(Category category) {
//        category.setCategoryId(categoryID++);
//        categories.add(category);
//    }
//
//    @Override
//    public String deleteCategory(Long categoryId) {
//        Category deleteCategory = categories.stream()
//                .filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst()
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
//
////        if (deleteCategory != null) {
////            categories.remove(deleteCategory);
////            return "Category with id: " + categoryId + " has been deleted";
////        }
////        return "No category found with id: " + categoryId;
//        categories.remove(deleteCategory);
//        return "Category with id: " + categoryId + " has been deleted";
//    }
//
//    @Override
//    public String updateCategory(Category category, Long categoryId) {
//        Optional<Category> optionalCategory = categories.stream()
//                .filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst();
//
//        if (optionalCategory.isPresent()) {
//            Category categoryToUpdate = optionalCategory.get();
//            categoryToUpdate.setCategoryName(category.getCategoryName());
//            String responseStr = "Category name updated with id: " + categoryId + "\nUpdated Name is " + categoryToUpdate.getCategoryName();
//            return responseStr;
//        } else {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
//        }
//
//    }

    // Below changes are done after using database and JPA

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Sorting
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        // Implementing pagination: below two lines are for fetching page wise
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageDetails);

        List<Category> categoryList = categoryPage.getContent();
//        List<Category> categoryList = categoryRepository.findAll();

        if (categoryList.isEmpty()) {
            throw new APIException("No categories have been created yet.");
        }

        List<CategoryDTO> matchingCategoriesList = categoryList.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(matchingCategoriesList);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getNumberOfElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO category) {
        Category categoryFromDB = categoryRepository.findByCategoryName(category.getCategoryName());

        if (categoryFromDB != null) {
            throw new APIException("Category with category name '" + category.getCategoryName() + "' already exist");
        }

        Category savedCategory = modelMapper.map(category, Category.class);
        Category response = categoryRepository.save(savedCategory);
        CategoryDTO responseDTO = modelMapper.map(response, CategoryDTO.class);
        return responseDTO;
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
//        List<Category> categories = getAllCategories();
//        Category deleteCategory = categories.stream()
//                .filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst()
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
//
//        categoryRepository.delete(deleteCategory);
//        return "Category with id: " + categoryId + " has been deleted";

        // Optimized
        Optional<Category> categoryToDeleteOptional = categoryRepository.findById(categoryId);
        Category categoryToDelete = categoryToDeleteOptional
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryRepository.delete(categoryToDelete);
        CategoryDTO deletedCategory = modelMapper.map(categoryToDelete, CategoryDTO.class);
//        return "Category with id: " + categoryId + " has been deleted";
        return deletedCategory;
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
//        List<Category> categories = getAllCategories();
//        Optional<Category> optionalCategory = categories.stream()
//                .filter(c -> c.getCategoryId().equals(categoryId))
//                .findFirst();
//
//        if (optionalCategory.isPresent()) {
//            Category categoryToUpdate = optionalCategory.get();
//            categoryToUpdate.setCategoryName(category.getCategoryName());
//            categoryRepository.save(categoryToUpdate);
//            String responseStr = "Category name updated with id: " + categoryId + "\nUpdated Name is " + categoryToUpdate.getCategoryName();
//            return responseStr;
//        } else {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
//        }

        // Optimized
        Optional<Category> categoryToUpdateOptional = categoryRepository.findById(categoryId);
//        Category categoryToUpdate = categoryToUpdateOptional
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Category categoryToUpdate = categoryToUpdateOptional
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryToUpdate.setCategoryName(categoryDTO.getCategoryName());
        Category updatedCategory = categoryRepository.save(categoryToUpdate);
        CategoryDTO updatedCategoryDTO = modelMapper.map(updatedCategory, CategoryDTO.class);
//        String responseStr = "Category name updated with id: " + categoryId + "\nUpdated Name is " + categoryToUpdate.getCategoryName();
//        return responseStr;
        return updatedCategoryDTO;
    }


}
