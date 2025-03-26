package com.ecommerce.project.repository;

import com.ecommerce.project.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // @Repository is optional because it extends JpaRepository. So when application context runs component scan, it identifies the interfaces and create implementation of CategoryRepository interface and register it as a bean at runtime.
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategoryName(String categoryName);
}
