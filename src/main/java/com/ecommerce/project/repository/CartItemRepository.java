package com.ecommerce.project.repository;

import com.ecommerce.project.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Here both the JPQL works because ci.product.id and ci.cart.id will point to the column marked with @Id
     */
//    @Query("SELECT ci FROM CartItem ci WHERE ci.product.productId = ?1 AND ci.cart.cartId = ?2")
    @Query("SELECT ci FROM CartItem ci WHERE ci.product.id = ?1 AND ci.cart.id = ?2")
    CartItem findCartItemByProductIdAndCartId(Long productId, Long cartId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
    void deleteCartItemByProductIdAndCartId(Long cartId, Long productId);
}


//public interface CartItemRepository extends JpaRepository<CartItem, Long> {
//    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.product.id = ?2")
//    CartItem findCartItemByProductIdAndCartId(Long cartId, Long productId);
//}