package com.victusstore.controller;

import com.victusstore.model.Product;
import com.victusstore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

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
                    if (productDetails.getIsActive() != null) product.setIsActive(productDetails.getIsActive());
                    Product updatedProduct = productRepository.save(product);
                    return ResponseEntity.ok(updatedProduct);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteProduct(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> {
                    productRepository.delete(product);
                    Map<String, Boolean> response = new HashMap<>();
                    response.put("deleted", Boolean.TRUE);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
