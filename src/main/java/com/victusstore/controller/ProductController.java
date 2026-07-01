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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAll(pageable);
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
        Product product = new Product();
        product.setProductName((String) payload.get("productName"));
        product.setDescription((String) payload.get("description"));
        product.setBasePrice(payload.get("basePrice") != null ? 
            new BigDecimal(payload.get("basePrice").toString()) : null);
        product.setSellerId(payload.get("sellerId") != null ? 
            Long.valueOf(payload.get("sellerId").toString()) : null);
        product.setIsActive(payload.get("isActive") != null ? 
            Boolean.valueOf(payload.get("isActive").toString()) : true);

        // Handle categories
        if (payload.get("categoryIds") != null) {
            @SuppressWarnings("unchecked")
            List<Integer> categoryIds = (List<Integer>) payload.get("categoryIds");
            Set<Category> categories = categoryIds.stream()
                .map(id -> categoryRepository.findById(id.longValue()))
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toSet());
            product.setCategories(categories);
        }

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        return productRepository.findById(id)
                .map(product -> {
                    if (payload.get("productName") != null) product.setProductName((String) payload.get("productName"));
                    if (payload.get("description") != null) product.setDescription((String) payload.get("description"));
                    if (payload.get("basePrice") != null) product.setBasePrice(new BigDecimal(payload.get("basePrice").toString()));
                    if (payload.get("sellerId") != null) product.setSellerId(Long.valueOf(payload.get("sellerId").toString()));
                    if (payload.get("isActive") != null) product.setIsActive(Boolean.valueOf(payload.get("isActive").toString()));
                    
                    // Handle categories - replace existing categories with new ones
                    if (payload.get("categoryIds") != null) {
                        @SuppressWarnings("unchecked")
                        List<Integer> categoryIds = (List<Integer>) payload.get("categoryIds");
                        Set<Category> categories = categoryIds.stream()
                            .map(catId -> categoryRepository.findById(catId.longValue()))
                            .filter(java.util.Optional::isPresent)
                            .map(java.util.Optional::get)
                            .collect(Collectors.toSet());
                        product.setCategories(categories);
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
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return ResponseEntity.ok(products);
    }
}
