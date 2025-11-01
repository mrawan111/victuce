package com.victusstore.controller;

import com.victusstore.model.CartProduct;
import com.victusstore.model.ProductVariant;
import com.victusstore.repository.CartProductRepository;
import com.victusstore.repository.CartRepository;
import com.victusstore.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart-products")
@CrossOrigin(origins = "*")
public class CartProductController {

    @Autowired
    private CartProductRepository cartProductRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @GetMapping
    public ResponseEntity<List<CartProduct>> getAllCartProducts() {
        return ResponseEntity.ok(cartProductRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCartProduct(@PathVariable Long id) {
        Optional<CartProduct> cartProduct = cartProductRepository.findById(id);
        if (cartProduct.isPresent()) {
            return ResponseEntity.ok(cartProduct.get());
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Cart product not found"));
        }
    }

    @GetMapping("/cart/{cartId}")
    public ResponseEntity<?> getCartProductsByCartId(@PathVariable Long cartId) {
        try {
            List<CartProduct> cartProducts = cartProductRepository.findByCartId(cartId);
            return ResponseEntity.ok(cartProducts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCartProduct(@RequestBody Map<String, Object> data) {
        try {
            if (!data.containsKey("variant_id") || !data.containsKey("cart_id") || !data.containsKey("quantity")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            Long variantId = Long.valueOf(data.get("variant_id").toString());
            Long cartId = Long.valueOf(data.get("cart_id").toString());
            Integer quantity = Integer.valueOf(data.get("quantity").toString());

            if (!cartRepository.existsById(cartId)) {
                return ResponseEntity.status(404).body(Map.of("error", "Cart not found"));
            }

            ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

            if (variant.getStockQuantity() < quantity) {
                return ResponseEntity.badRequest().body(Map.of("error", "Insufficient stock"));
            }

            Optional<CartProduct> existingOpt = cartProductRepository.findByCartIdAndVariantId(cartId, variantId);

            Long cartProductId;
            if (existingOpt.isPresent()) {
                CartProduct existing = existingOpt.get();
                existing.setQuantity(existing.getQuantity() + quantity);
                cartProductRepository.save(existing);
                cartProductId = existing.getId();
            } else {
                CartProduct cartProduct = new CartProduct();
                cartProduct.setCartId(cartId);
                cartProduct.setVariantId(variantId);
                cartProduct.setQuantity(quantity);
                cartProduct.setPriceAtTime(variant.getPrice());
                CartProduct saved = cartProductRepository.save(cartProduct);
                cartProductId = saved.getId();
            }

            return ResponseEntity.status(201).body(Map.of(
                "message", "Product added to cart successfully",
                "cart_product_id", cartProductId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProductQuantity(@RequestBody Map<String, Object> data) {
        try {
            if (!data.containsKey("cart_id") || !data.containsKey("variant_id") || !data.containsKey("quantity")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
            }

            Long cartId = Long.valueOf(data.get("cart_id").toString());
            Long variantId = Long.valueOf(data.get("variant_id").toString());
            Integer quantity = Integer.valueOf(data.get("quantity").toString());

            ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

            if (quantity > variant.getStockQuantity()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Requested quantity exceeds available stock"));
            }

            CartProduct cartProduct = cartProductRepository.findByCartIdAndVariantId(cartId, variantId)
                .orElseThrow(() -> new RuntimeException("Cart product not found"));

            cartProduct.setQuantity(quantity);
            cartProductRepository.save(cartProduct);

            return ResponseEntity.ok(Map.of("message", "Product quantity updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCartProduct(@PathVariable Long id) {
        try {
            cartProductRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Cart product deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
