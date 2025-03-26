package com.ecommerce.project.controller;

import com.ecommerce.project.config.AppConstants;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    ProductService productService;

    @PostMapping("admin/categories/{categoryId}/products")
    public ResponseEntity<ProductDTO> addProduct (@Valid @RequestBody ProductDTO productDTO,
                                                  @PathVariable Long categoryId) {
        ProductDTO savedProductDTO = productService.addProduct(productDTO, categoryId);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    @GetMapping("public/products")
    public ResponseEntity<ProductResponse> getAllProducts(@RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                          @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                          @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                          @RequestParam(defaultValue = AppConstants.SORT_ORDER) String sortOrder) {
        ProductResponse response = productService.getAllProducts(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getProductsByCategory(@PathVariable Long categoryId,
                                                                 @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                 @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                 @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                                 @RequestParam(defaultValue = AppConstants.SORT_ORDER) String sortOrder) {
        ProductResponse response = productService.searchByCategory(categoryId, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getProductsByKeyword (@PathVariable String keyword,
                                                                 @RequestParam(defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                 @RequestParam(defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                 @RequestParam(defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                                 @RequestParam(defaultValue = AppConstants.SORT_ORDER) String sortOrder) {
        ProductResponse productResponse = productService.searchProductByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @PutMapping("admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct (@PathVariable Long productId, @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO updateProductDTO = productService.updateProduct(productId, productDTO);
        return new ResponseEntity<>(updateProductDTO, HttpStatus.OK);
    }

    @DeleteMapping("admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct (@PathVariable Long productId) {
        ProductDTO productDTO = productService.deleteProduct(productId);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }

    @PutMapping("admin/products/{productId}/image")
    public ResponseEntity<ProductDTO> uploadProductImage (@PathVariable Long productId,
                                                          @RequestParam MultipartFile image) throws IOException {
        System.out.println("here");
        ProductDTO productDTO = productService.updateProductImage(productId, image);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }
}
