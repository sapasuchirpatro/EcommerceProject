package com.ecommerce.project.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity(name = "category") // If name provided then the table will be created with this name otherwise table will be created with the class name
@Data
@NoArgsConstructor
@AllArgsConstructor
// We need added @NoArgsConstructor and @AllArgsConstructor. But @RequiredArgsConstructor is part of @Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotBlank
    @Size(min = 5, message = "Size can't be less than 5")
    private String categoryName;

    // Bidirectional relationship
    @ToString.Exclude
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;

    // Constructor, getters and setter are commented because now we are using lombok
//    public Category(Long categoryId, String categoryName) {
//        this.categoryId = categoryId;
//        this.categoryName = categoryName;
//    }
//
//    public Category() {
//    }
//
//    public Long getCategoryId() {
//        return categoryId;
//    }
//
//    public void setCategoryId(Long categoryId) {
//        this.categoryId = categoryId;
//    }
//
//    public String getCategoryName() {
//        return categoryName;
//    }
//
//    public void setCategoryName(String categoryName) {
//        this.categoryName = categoryName;
//    }
}
