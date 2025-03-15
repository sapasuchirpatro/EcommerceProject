package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    CartService cartService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    FilesService filesService;

    @Value("${project.image}")
    String path;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

//        Product alreadySavedProduct = productRepository.findByProductName(productDTO.getProductName());
//        if (alreadySavedProduct != null) {
//            throw new APIException("Product with product name \"" + productDTO.getProductName() + "\" already exist");
//        }

        // check product already product exists
        List<Product> savedProducts = category.getProducts(); // this is coming because of bidirectional relationship in category Entity
        for(Product product : savedProducts) {
            if(product.getProductName().equalsIgnoreCase(productDTO.getProductName())) {
                throw new APIException("Product with product name \"" + productDTO.getProductName() + "\" already exist");
            }
        }

        Product product = modelMapper.map(productDTO, Product.class);

        product.setImage("default.png");
        product.setCategory(category);
        //specialPrice (discountPrice) =  price - (price * (discount percentage / 100))
        double specialPrice = product.getPrice() - (product.getPrice() * (product.getDiscount() / 100));
        product.setSpecialPrice(specialPrice);

        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

//    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Sorting
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        // Implementing pagination: below two lines are for fetching page wise
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findAll(pageDetails);

        List<Product> products = productPage.getContent();
        if (products.isEmpty()) {
            throw new APIException("No products to show");
        }

        List<ProductDTO> productDTOList = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

//        ProductResponse productResponse = new ProductResponse();
//        productResponse.setContent(productDTOList);
//        return productResponse;

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }


    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        ProductResponse productResponse = new ProductResponse();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Sorting
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        // Implementing pagination: below two lines are for fetching page wise
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findByCategory(category, pageDetails);

        List<Product> products = productPage.getContent();

//        List<Product> products = productRepository.findByCategory(category);

        if (products.isEmpty()) {
            throw new APIException("No products found under category \"" + category.getCategoryName() + "\"");
        }

        List<ProductDTO> productDTOList = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        productResponse.setContent(productDTOList);
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        // Sorting
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending() :
                Sort.by(sortBy).descending();

        // Implementing pagination: below two lines are for fetching page wise
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase("%" + keyword + "%", pageDetails);

        List<Product> products = productPage.getContent();

//        List<Product> products = productRepository.findByProductNameLikeIgnoreCase("%" + keyword + "%");

        if (products.isEmpty()) {
            throw new APIException("No products found matching \"" + keyword + "\"");
        }

        List<ProductDTO> productDTOList = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();
        productResponse.setContent(productDTOList);
        productResponse.setContent(productDTOList);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
//        Optional<Product> savedProductOptional = productRepository.findById(productId);
//        Product savedProduct = savedProductOptional.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Product savedProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Product product = modelMapper.map(productDTO, Product.class);

        savedProduct.setProductName(product.getProductName());
        savedProduct.setDescription(product.getDescription());
        savedProduct.setQuantity(product.getQuantity());
        savedProduct.setPrice(product.getPrice());
        savedProduct.setDiscount(product.getDiscount());
        double specialPrice = product.getPrice() - (product.getPrice() * (product.getDiscount() / 100));
        savedProduct.setSpecialPrice(specialPrice);
        productRepository.save(savedProduct);


        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream()
                .map(cart -> {
                    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

                    List<ProductDTO> products = cart.getCartItems().stream()
                            .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

                    cartDTO.setProducts(products);

                    return cartDTO;
                })
                .toList();

        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

        ProductDTO updatedProduct = modelMapper.map(savedProduct, ProductDTO.class);
        return updatedProduct;
    }

//    @Override
//    public ProductDTO deleteProduct(Long productId) {
//        Optional<Product> productToDelete = productRepository.findById(productId);
//
//        if (productToDelete.isEmpty()) {
//            throw new ResourceNotFoundException("Product", "productId", productId);
//        }
//
//        // DELETE
//        List<Cart> carts = cartRepository.findCartsByProductId(productId);
//        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));
//
//        productRepository.deleteById(productId);
//
//        Product deletedProduct = productToDelete.orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
//        return modelMapper.map(deletedProduct, ProductDTO.class);
//    }

    @Override
    public ProductDTO deleteProduct(Long productId) {
        // Fetch the product to delete
        Product productToDelete = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Fetch the carts containing this product
        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        // Remove the product from each cart
        for (Cart cart : carts) {
            cart.getCartItems().removeIf(cartItem -> cartItem.getProduct().getProductId().equals(productId));
            cartRepository.save(cart); // explicitly save the cart to ensure proper cascades
        }

        // Delete the product after removing from cart
        productRepository.delete(productToDelete);

        // Return a mapped DTO for deleted product
        return modelMapper.map(productToDelete, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage (Long productId, MultipartFile image) throws IOException {

        // Get product from DB
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        // Upload an image to server
        // Get the file name of the uploaded image
//        String path = "images/";
        String fileName = filesService.uploadImage(path, image);

        // Update the new file name to the product
        product.setImage(fileName);
        Product updatedProduct = productRepository.save(product);

        // return the DTO after mapping product to DTO.
        return modelMapper.map(updatedProduct, ProductDTO.class);
    }
}
