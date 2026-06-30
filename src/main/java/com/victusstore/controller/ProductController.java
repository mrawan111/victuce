package com.victusstore.controller;

import com.victusstore.model.Product;
import com.victusstore.model.ProductVariant;
import com.victusstore.model.CartProduct;
import com.victusstore.repository.CartProductRepository;
import com.victusstore.repository.CartRepository;
import com.victusstore.repository.ProductRepository;
import com.victusstore.repository.ProductVariantRepository;
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
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productRepository.findById(id)
                .map(product -> {
                    if (productDetails.getProductName() != null) product.setProductName(productDetails.getProductName());
                    if (productDetails.getDescription() != null) product.setDescription(productDetails.getDescription());
                    if (productDetails.getBasePrice() != null) product.setBasePrice(productDetails.getBasePrice());
                    if (productDetails.getCategoryId() != null) product.setCategoryId(productDetails.getCategoryId());
                    if (productDetails.getSellerId() != null) product.setSellerId(productDetails.getSellerId());
                    if (productDetails.getIsActive() != null) product.setIsActive(productDetails.getIsActive());
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
