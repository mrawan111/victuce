package com.victusstore.controller;

import com.victusstore.model.CartProduct;
import com.victusstore.model.ProductVariant;
import com.victusstore.model.Product;
import com.victusstore.repository.CartProductRepository;
import com.victusstore.repository.CartRepository;
import com.victusstore.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart-products")
@CrossOrigin(origins = "*")
public class CartProductController {

    private static final Logger logger = LoggerFactory.getLogger(CartProductController.class);

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

    /**
     * ⭐ ENHANCED: Add Product to Cart with Full Validation and Detailed Response
     * 
     * This endpoint now:
     * 1. Validates all inputs thoroughly
     * 2. Returns detailed product information for frontend confirmation
     * 3. Provides clear error messages
     * 4. Helps prevent wrong products from being added
     */
    @PostMapping
    public ResponseEntity<?> createCartProduct(@RequestBody Map<String, Object> data) {
        try {
            logger.info("POST /api/cart-products called with payload: {}", data);
            // Validate required fields
            if (!data.containsKey("variant_id") || !data.containsKey("cart_id") || !data.containsKey("quantity")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Missing required fields",
                    "required_fields", List.of("variant_id", "cart_id", "quantity")
                ));
            }

            Long variantId = Long.valueOf(data.get("variant_id").toString());
            Long cartId = Long.valueOf(data.get("cart_id").toString());
            Integer quantity = Integer.valueOf(data.get("quantity").toString());

            // Validate quantity
            if (quantity <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Quantity must be greater than 0"
                ));
            }

            // Validate cart exists
            if (!cartRepository.existsById(cartId)) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Cart not found",
                    "cart_id", cartId
                ));
            }

            // ⭐ ENHANCEMENT: Fetch variant with full product details
            ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found for ID: " + variantId));

            // ⭐ ENHANCEMENT: Validate variant is active
            if (variant.getIsActive() != null && !variant.getIsActive()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "This product variant is no longer available",
                    "variant_id", variantId
                ));
            }


            // ⭐ ENHANCEMENT: Get product details for response
            Product product = variant.getProduct();
            if (product != null && product.getIsActive() != null && !product.getIsActive()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "This product is no longer available",
                    "product_id", product.getProductId()
                ));
            }

            // Validate stock
            if (variant.getStockQuantity() < quantity) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Insufficient stock",
                    "available_stock", variant.getStockQuantity(),
                    "requested_quantity", quantity,
                    "product_name", product != null ? product.getProductName() : "Unknown",
                    "variant_details", Map.of(
                        "color", variant.getColor(),
                        "size", variant.getSize()
                    )
                ));
            }

            // Calculate priceAtTime as base price + variant price
            java.math.BigDecimal priceAtTime = java.math.BigDecimal.ZERO;
            if (product != null && product.getBasePrice() != null && variant.getPrice() != null) {
                priceAtTime = product.getBasePrice().add(variant.getPrice());
            } else if (variant.getPrice() != null) {
                priceAtTime = variant.getPrice();
            }

            // Check if variant already exists in cart
            Optional<CartProduct> existingOpt = cartProductRepository.findByCartIdAndVariantId(cartId, variantId);

            Long cartProductId;
            Integer newQuantity;
            boolean isNewItem;

            if (existingOpt.isPresent()) {
                // Update existing cart item
                CartProduct existing = existingOpt.get();
                Integer totalQuantity = existing.getQuantity() + quantity;
                // Validate total quantity against stock
                if (totalQuantity > variant.getStockQuantity()) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", "Insufficient stock for requested quantity",
                        "current_in_cart", existing.getQuantity(),
                        "trying_to_add", quantity,
                        "total_requested", totalQuantity,
                        "available_stock", variant.getStockQuantity()
                    ));
                }
                existing.setQuantity(totalQuantity);
                // Update priceAtTime if needed
                existing.setPriceAtTime(priceAtTime);
                cartProductRepository.save(existing);
                cartProductId = existing.getId();
                newQuantity = totalQuantity;
                isNewItem = false;
            } else {
                // Add new cart item
                CartProduct cartProduct = new CartProduct();
                cartProduct.setCartId(cartId);
                cartProduct.setVariantId(variantId);
                cartProduct.setQuantity(quantity);
                cartProduct.setPriceAtTime(priceAtTime);
                CartProduct saved = cartProductRepository.save(cartProduct);
                cartProductId = saved.getId();
                newQuantity = quantity;
                isNewItem = true;
            }

            // ⭐ ENHANCEMENT: Return detailed response with product information
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", isNewItem ? "Product added to cart successfully" : "Cart quantity updated successfully");
            response.put("cart_product_id", cartProductId);
            response.put("is_new_item", isNewItem);
            response.put("quantity", newQuantity);

            // Include product details for frontend confirmation
            Map<String, Object> productInfo = new HashMap<>();
            if (product != null) {
                productInfo.put("product_id", product.getProductId());
                productInfo.put("product_name", product.getProductName());
                productInfo.put("description", product.getDescription());
                productInfo.put("base_price", product.getBasePrice());
            }
            productInfo.put("variant_id", variant.getVariantId());
            productInfo.put("color", variant.getColor());
            productInfo.put("size", variant.getSize());
            productInfo.put("price", variant.getPrice());
            productInfo.put("sku", variant.getSku());
            productInfo.put("price_at_time", priceAtTime);

            response.put("product_details", productInfo);

            return ResponseEntity.status(201).body(response);

        } catch (NumberFormatException e) {
            logger.warn("NumberFormatException while adding to cart: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid number format",
                "details", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Exception while adding to cart", e);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to add product to cart",
                "details", e.getMessage()
            ));
        }
    }

    /**
     * ⭐ ENHANCED: Update Product Quantity with Validation
     */
    @PutMapping
    public ResponseEntity<?> updateProductQuantity(@RequestBody Map<String, Object> data) {
        try {
            if (!data.containsKey("cart_id") || !data.containsKey("variant_id") || !data.containsKey("quantity")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Missing required fields",
                    "required_fields", List.of("cart_id", "variant_id", "quantity")
                ));
            }

            Long cartId = Long.valueOf(data.get("cart_id").toString());
            Long variantId = Long.valueOf(data.get("variant_id").toString());
            Integer quantity = Integer.valueOf(data.get("quantity").toString());

            // Validate quantity
            if (quantity <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Quantity must be greater than 0"
                ));
            }

            // Fetch variant with product details
            ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));

            // Validate stock
            if (quantity > variant.getStockQuantity()) {
                Product product = variant.getProduct();
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Requested quantity exceeds available stock",
                    "product_name", product != null ? product.getProductName() : "Unknown",
                    "requested_quantity", quantity,
                    "available_stock", variant.getStockQuantity(),
                    "variant_details", Map.of(
                        "color", variant.getColor(),
                        "size", variant.getSize()
                    )
                ));
            }

            // Find and update cart product
            CartProduct cartProduct = cartProductRepository.findByCartIdAndVariantId(cartId, variantId)
                .orElseThrow(() -> new RuntimeException("Cart product not found"));

            Integer oldQuantity = cartProduct.getQuantity();
            cartProduct.setQuantity(quantity);
            cartProductRepository.save(cartProduct);

            // Return detailed response
            Product product = variant.getProduct();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Product quantity updated successfully");
            response.put("old_quantity", oldQuantity);
            response.put("new_quantity", quantity);
            response.put("product_name", product != null ? product.getProductName() : "Unknown");
            response.put("variant_details", Map.of(
                "color", variant.getColor(),
                "size", variant.getSize()
            ));

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid number format",
                "details", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to update cart product",
                "details", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCartProduct(@PathVariable Long id) {
        try {
            Optional<CartProduct> cartProductOpt = cartProductRepository.findById(id);
            if (cartProductOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                    "error", "Cart product not found",
                    "id", id
                ));
            }

            // Get product details before deletion for response
            CartProduct cartProduct = cartProductOpt.get();
            ProductVariant variant = cartProduct.getVariant();
            String productName = "Unknown";
            if (variant != null && variant.getProduct() != null) {
                productName = variant.getProduct().getProductName();
            }

            cartProductRepository.deleteById(id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cart product deleted successfully",
                "deleted_product", productName
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to delete cart product",
                "details", e.getMessage()
            ));
        }
    }
}
