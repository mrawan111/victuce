package com.victusstore.repository;

import com.victusstore.model.CartProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartProductRepository extends JpaRepository<CartProduct, Long> {
    List<CartProduct> findByCartId(Long cartId);
    Optional<CartProduct> findByCartIdAndVariantId(Long cartId, Long variantId);
    List<CartProduct> findByOrderId(Long orderId);
}
