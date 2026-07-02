package com.victusstore.controller;

import com.victusstore.model.Product;
import com.victusstore.model.Category;
import com.victusstore.model.ProductVariant;
import com.victusstore.model.CartProduct;
import com.victusstore.repository.CartProductRepository;
import com.victusstore.repository.CartRepository;
import com.victusstore.repository.ProductRepository;
import com.victusstore.repository.ProductVariantRepository;
import com.victusstore.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private CartProductRepository cartProductRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products;
        if (categoryId != null) {
            // Use inheritance to get products from category and all its descendants
            List<Long> productIds = productRepository.findProductIdsByCategoryIdWithInheritance(categoryId);
            List<Product> allProducts = productRepository.findAllById(productIds);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allProducts.size());
            List<Product> pageContent = allProducts.subList(start, end);
            products = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, allProducts.size()
            );
        } else {
            products = productRepository.findAll(pageable);
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(product))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Map<String, Object> payload) {
        Long categoryId = extractCategoryId(payload);
        if (categoryId == null) {
            return ResponseEntity.<Product>badRequest().build();
        }

        Product product = new Product();
        product.setProductName((String) payload.get("productName"));
        product.setDescription((String) payload.get("description"));
        product.setBasePrice(payload.get("basePrice") != null ? 
            new BigDecimal(payload.get("basePrice").toString()) : null);
        product.setSellerId(payload.get("sellerId") != null ? 
            Long.valueOf(payload.get("sellerId").toString()) : null);
        product.setIsActive(payload.get("isActive") != null ? 
            Boolean.valueOf(payload.get("isActive").toString()) : true);

        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            return ResponseEntity.<Product>badRequest().build();
        }
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return productRepository.findById(id)
                .map(product -> {
                    if (payload.get("productName") != null) product.setProductName((String) payload.get("productName"));
                    if (payload.get("description") != null) product.setDescription((String) payload.get("description"));
                    if (payload.get("basePrice") != null) product.setBasePrice(new BigDecimal(payload.get("basePrice").toString()));
                    if (payload.get("sellerId") != null) product.setSellerId(Long.valueOf(payload.get("sellerId").toString()));
                    if (payload.get("isActive") != null) product.setIsActive(Boolean.valueOf(payload.get("isActive").toString()));

                    Long categoryId = extractCategoryId(payload);
                    if (payload.containsKey("categoryId")) {
                        if (categoryId == null) {
                            return ResponseEntity.<Product>badRequest().build();
                        }
                        Category category = categoryRepository.findById(categoryId).orElse(null);
                        if (category == null) {
                            return ResponseEntity.<Product>badRequest().build();
                        }
                        product.setCategory(category);
                    }
                    
                    Product updatedProduct = productRepository.save(product);
                    return ResponseEntity.ok(updatedProduct);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Map<String, Boolean>> deleteProduct(@PathVariable Long id) {
        try {
            if (!productRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            List<Long> variantIds = variantRepository.findByProductId(id).stream()
                    .map(ProductVariant::getVariantId)
                    .collect(Collectors.toList());
            if (!variantIds.isEmpty()) {
                List<CartProduct> activeCartItems =
                        cartProductRepository.findByVariantIdInAndOrderIdIsNull(variantIds);
                Set<Long> affectedCartIds = activeCartItems.stream()
                        .map(CartProduct::getCartId)
                        .collect(Collectors.toSet());

                cartProductRepository.deleteByVariantIdInAndOrderIdIsNull(variantIds);
                affectedCartIds.forEach(this::recalculateCartTotal);
            }

            productRepository.deleteById(id);
            Map<String, Boolean> response = new HashMap<>();
            response.put("deleted", Boolean.TRUE);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Boolean> response = new HashMap<>();
            response.put("deleted", Boolean.FALSE);
            response.put("error", true);
            return ResponseEntity.badRequest().body(response);
        }
    }

    private void recalculateCartTotal(Long cartId) {
        cartRepository.findById(cartId).ifPresent(cart -> {
            BigDecimal totalPrice = cartProductRepository.findByCartId(cartId).stream()
                    .map(item -> item.getPriceAtTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            cart.setTotalPrice(totalPrice);
            cartRepository.save(cart);
        });
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Use inheritance to get products from category and all its descendants
        List<Long> productIds = productRepository.findProductIdsByCategoryIdWithInheritance(categoryId);
        List<Product> allProducts = productRepository.findAllById(productIds);
        
        // Manual pagination for the result
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allProducts.size());
        List<Product> pageContent = allProducts.subList(start, end);
        
        Page<Product> products = new org.springframework.data.domain.PageImpl<>(
            pageContent, pageable, allProducts.size()
        );
        return ResponseEntity.ok(products);
    }

    private Long extractCategoryId(Map<String, Object> payload) {
        if (!payload.containsKey("categoryId")) {
            return null;
        }

        Object rawCategoryId = payload.get("categoryId");
        if (rawCategoryId == null) {
            return null;
        }

        try {
            return Long.valueOf(rawCategoryId.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
