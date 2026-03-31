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
public class CartProductController {

    private static final Logger logger = LoggerFactory.getLogger(CartProductController.class);

    @Autowired
    private CartProductRepository cartProductRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    /**
     * In this project, variant.price is the final sell price for that option.
     * It should not be added on top of product.basePrice.
     */
    private java.math.BigDecimal resolvePriceAtTime(ProductVariant variant, Product product) {
        try {
            if (variant != null && variant.getPrice() != null) {
                return variant.getPrice();
            }
            if (product != null && product.getBasePrice() != null) {
                return product.getBasePrice();
            }
        } catch (Exception e) {
            logger.error("Could not resolve priceAtTime: {}", e.getMessage());
        }
        return java.math.BigDecimal.ZERO;
    }

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
     * 5. Handles database constraints gracefully
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

            // ⭐ ENHANCEMENT: Validate variant is active (skip if isActive method not available)
            boolean isActive = true;
            try {
                isActive = variant.getIsActive() != null ? variant.getIsActive() : true;
            } catch (Exception e) {
                // Field might not exist or be accessible
                logger.debug("Could not check isActive status for variant: {}", e.getMessage());
            }
            
            if (!isActive) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "This product variant is no longer available",
                    "variant_id", variantId
                ));
            }

            // ⭐ ENHANCEMENT: Get product details for response
            Product product = null;
            try {
                product = variant.getProduct();
                if (product != null) {
                    boolean productActive = true;
                    try {
                        productActive = product.getIsActive() != null ? product.getIsActive() : true;
                    } catch (Exception e) {
                        logger.debug("Could not check isActive status for product: {}", e.getMessage());
                    }
                    
                    if (!productActive) {
                        return ResponseEntity.badRequest().body(Map.of(
                            "error", "This product is no longer available",
                            "product_id", product.getProductId()
                        ));
                    }
                }
            } catch (Exception e) {
                logger.debug("Could not get product from variant: {}", e.getMessage());
            }

            // Validate stock
            Integer availableStock = 0;
            try {
                availableStock = variant.getStockQuantity();
            } catch (Exception e) {
                logger.error("Could not get stock quantity for variant: {}", e.getMessage());
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Could not verify stock availability"
                ));
            }
            
            if (availableStock < quantity) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Insufficient stock",
                    "available_stock", availableStock,
                    "requested_quantity", quantity,
                    "product_name", product != null ? product.getProductName() : "Unknown",
                    "variant_details", Map.of(
                        "color", variant.getColor(),
                        "size", variant.getSize()
                    )
                ));
            }

            java.math.BigDecimal priceAtTime = resolvePriceAtTime(variant, product);

            // Upsert cart item (prevent duplicates for same cart + variant)
            Long cartProductId;
            Integer newQuantity;
            boolean isNewItem;

            List<CartProduct> existingItems = cartProductRepository.findAllByCartIdAndVariantId(cartId, variantId);
            CartProduct cartProduct;

            if (existingItems != null && !existingItems.isEmpty()) {
                cartProduct = existingItems.get(0);
                newQuantity = cartProduct.getQuantity() + quantity;
                cartProduct.setQuantity(newQuantity);
                cartProduct.setPriceAtTime(priceAtTime);
                isNewItem = false;
            } else {
                cartProduct = new CartProduct();
                cartProduct.setCartId(cartId);
                cartProduct.setVariantId(variantId);
                cartProduct.setQuantity(quantity);
                cartProduct.setPriceAtTime(priceAtTime);
                newQuantity = quantity;
                isNewItem = true;
            }

            try {
                CartProduct saved = cartProductRepository.save(cartProduct);
                cartProductId = saved.getId();

                // Cleanup any duplicate rows that might exist
                if (existingItems != null && existingItems.size() > 1) {
                    for (int i = 1; i < existingItems.size(); i++) {
                        cartProductRepository.deleteById(existingItems.get(i).getId());
                    }
                }
            } catch (Exception e) {
                logger.error("Failed to save cart product: {}", e.getMessage());
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to add product to cart",
                    "details", e.getMessage()
                ));
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
                try {
                    productInfo.put("product_id", product.getProductId());
                    productInfo.put("product_name", product.getProductName());
                    productInfo.put("description", product.getDescription());
                    productInfo.put("base_price", product.getBasePrice());
                } catch (Exception e) {
                    logger.debug("Could not extract product info: {}", e.getMessage());
                }
            }
            
            try {
                productInfo.put("variant_id", variant.getVariantId());
                productInfo.put("color", variant.getColor());
                productInfo.put("size", variant.getSize());
                productInfo.put("price", variant.getPrice());
                productInfo.put("sku", variant.getSku());
                productInfo.put("price_at_time", priceAtTime);
            } catch (Exception e) {
                logger.debug("Could not extract variant info: {}", e.getMessage());
            }

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

            Product product = variant.getProduct();
            java.math.BigDecimal priceAtTime = resolvePriceAtTime(variant, product);

            // Find and update cart product(s). If the item exists only in local cart,
            // create the backend row instead of failing the quantity update.
            List<CartProduct> cartProducts = cartProductRepository.findAllByCartIdAndVariantId(cartId, variantId);
            CartProduct primary;
            Integer oldQuantity;
            boolean createdMissingItem = false;

            if (cartProducts == null || cartProducts.isEmpty()) {
                primary = new CartProduct();
                primary.setCartId(cartId);
                primary.setVariantId(variantId);
                primary.setQuantity(quantity);
                primary.setPriceAtTime(priceAtTime);
                primary = cartProductRepository.save(primary);
                oldQuantity = 0;
                createdMissingItem = true;
                cartProducts = List.of(primary);
            } else {
                primary = cartProducts.get(0);
                oldQuantity = primary.getQuantity();
                primary.setQuantity(quantity);
                primary.setPriceAtTime(priceAtTime);
                cartProductRepository.save(primary);
            }

            // Cleanup duplicates if any
            if (cartProducts.size() > 1) {
                for (int i = 1; i < cartProducts.size(); i++) {
                    cartProductRepository.deleteById(cartProducts.get(i).getId());
                }
            }

            // Return detailed response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", createdMissingItem ? "Product added to cart successfully" : "Product quantity updated successfully");
            response.put("old_quantity", oldQuantity);
            response.put("new_quantity", quantity);
            response.put("created_missing_item", createdMissingItem);
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
