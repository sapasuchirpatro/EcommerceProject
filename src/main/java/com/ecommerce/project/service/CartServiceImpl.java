package com.ecommerce.project.service;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    ModelMapper modelMapper;

    /**
     * Adds a specific product with a defined quantity to the user's cart.
     * If the cart doesn't exist, it creates a new one.
     *
     * @param productId The ID of the product to add to the cart.
     * @param quantity  The quantity of the product to add.
     * @return The updated cart details as a CartDTO.
     * @throws APIException              If the product is out of stock or already exists in the cart.
     * @throws ResourceNotFoundException If the product with the given ID is not found.
     */
    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // Find existing cart or create one if cart doesn't exist
        Cart cart = getCartDetails();

        System.out.println("cart: " + cart);

        // Retrieve product details using product id
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
//        ProductDTO productDTO = modelMapper.map(product, ProductDTO.class);


        // Perform validation (like if product exist in cart then throw validation)
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());

        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is out of stock");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Only " + product.getQuantity() + " left in stock");
        }

        // Create Cart Item and save it
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setQuantity(quantity);
        newCartItem.setCart(cart);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());
        CartItem savedCartItem = cartItemRepository.save(newCartItem);




        // Update the cart and Return the updated cart
        Double newCartTotalPrice = cart.getTotalPrice() + (product.getSpecialPrice() * quantity);
        cart.setTotalPrice(newCartTotalPrice);
        Cart updatedCart = cartRepository.save(cart);
        CartDTO cartDTO = modelMapper.map(updatedCart, CartDTO.class);

        List<CartItem> cartItems = updatedCart.getCartItems();
        cartItems.add(savedCartItem);

        System.out.println("cartItems: " + cartItems);

        Stream<ProductDTO> productDTOStream = cartItems.stream()
                .map(item -> {
                    ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
                    map.setQuantity(item.getQuantity());
                    return map;
                });

        cartDTO.setProducts(productDTOStream.toList());

        return cartDTO;
    }

    /**
     * Retrieves all carts from the database.
     *
     * @return A list of CartDTO objects containing details of all carts.
     * @throws APIException If no carts are found in the system.
     */
    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty()) {
            throw new APIException("No carts exists");
        }

        List<CartDTO> cartDTOS = carts.stream()
                .map(cart -> {
                    CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
                    List<ProductDTO> productDTO = cart.getCartItems().stream()
                                    .map(cartItem -> {
                                        cartItem.getProduct().setQuantity(cartItem.getQuantity());
                                        return modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                                    })
                                    .toList();
                    cartDTO.setProducts(productDTO);
                    return cartDTO;
                })
                .toList();
        return cartDTOS;
    }

    /**
     * Retrieves the details of the logged-in user's cart.
     * If no cart exists, a new cart is created and returned.
     *
     * @return The cart details of the logged-in user.
     */
    private Cart getCartDetails() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());

        if (userCart != null) {
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart =  cartRepository.save(cart);
        return newCart;
    }


    /**
     * Retrieves the cart details of the currently logged-in user.
     *
     * @return The cart details as a CartDTO.
     * @throws APIException If no cart exists for the logged-in user.
     */
    public CartDTO getCart() {
        String userEmail = authUtil.loggedInEmail();
        Cart cart = cartRepository.findCartByEmail(userEmail);

        if (cart == null) {
            throw new APIException("No cart exist for this user");
        }

        // In future if we have multiple types of cart for a single user then in that case we may need to pass cartId
        cart = cartRepository.findCartByEmailAndCartId(userEmail, cart.getCartId());


        cart.getCartItems()
                .forEach(cartItem -> cartItem.getProduct().setQuantity(cartItem.getQuantity()));

        List<ProductDTO> products = cart.getCartItems().stream()
                        .map(cartItem -> modelMapper.map(cartItem.getProduct(), ProductDTO.class))
                        .toList();

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cartDTO.setProducts(products);
        return cartDTO;
    }

    /**
     * Updates the quantity of a specific product in the user's cart.
     * If the product's quantity is set to zero, the item is removed from the cart.
     *
     * @param productId The ID of the product to update.
     * @param quantity  The new quantity of the product.
     * @return The updated cart details as a CartDTO.
     * @throws APIException              If the product is not available or quantity exceeds stock.
     * @throws ResourceNotFoundException If the product or cart is not found.
     */
    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {

        String emailId = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId  = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        int newQuantity = cartItem.getQuantity() + quantity;

        if (newQuantity < 0) {
            throw new APIException("The resulting quantity cannot be negative.");
        }

        if (newQuantity == 0) {
            deleteProductFromCart(cartId, productId);
        } else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(cart);
        }

        CartItem updatedItem = cartItemRepository.save(cartItem);

//        System.out.println( "updatedItem: " + updatedItem);
//        System.out.println( "cart: " + cart);
//
//        if(updatedItem.getQuantity() == 0){
//            cartItemRepository.deleteById(updatedItem.getCartItemId());
//        }

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    /**
     * Deletes a specific product from the user's cart.
     *
     * @param cartId    The ID of the cart from which the product is to be removed.
     * @param productId The ID of the product to remove from the cart.
     * @return A string message confirming the removal of the product from the cart.
     * @throws ResourceNotFoundException If the cart or product is not found.
     */
    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() -
                (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice()
                - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice
                + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepository.save(cartItem);
    }
    
}
