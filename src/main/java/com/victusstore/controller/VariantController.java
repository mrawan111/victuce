package com.victusstore.controller;

import com.victusstore.model.ProductVariant;
import com.victusstore.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/variants")
public class VariantController {
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariant(@PathVariable Long id, @RequestBody ProductVariant updatedVariant) {
        Optional<ProductVariant> variantOpt = variantRepository.findById(id);
        if (variantOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Variant not found"));
        }
        ProductVariant variant = variantOpt.get();
        // Update fields
        variant.setColor(updatedVariant.getColor());
        variant.setSize(updatedVariant.getSize());
        variant.setStockQuantity(updatedVariant.getStockQuantity());
        variant.setPrice(updatedVariant.getPrice());
        variant.setSku(updatedVariant.getSku());
        variant.setIsActive(updatedVariant.getIsActive());
        variant.setProductId(updatedVariant.getProductId());
        variant.setUpdatedAt(java.time.LocalDateTime.now());
        ProductVariant saved = variantRepository.save(variant);
        return ResponseEntity.ok(saved);
    }

    @Autowired
    private ProductVariantRepository variantRepository;

    @PostMapping
    public ResponseEntity<?> createVariant(@RequestBody ProductVariant variant) {
        try {
            variant.setVariantId(null); // Ensure ID is not set by client
            variant.setCreatedAt(java.time.LocalDateTime.now());
            variant.setUpdatedAt(java.time.LocalDateTime.now());
            ProductVariant saved = variantRepository.save(variant);
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductVariant>> getAllVariants() {
        return ResponseEntity.ok(variantRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVariant(@PathVariable Long id) {
        Optional<ProductVariant> variant = variantRepository.findById(id);
        if (variant.isPresent()) {
            return ResponseEntity.ok(variant.get());
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Variant not found"));
        }
    }

    @GetMapping("/{id}/check-availability")
    public ResponseEntity<?> checkAvailability(@PathVariable Long id) {
        try {
            Optional<ProductVariant> variantOpt = variantRepository.findById(id);
            if (variantOpt.isPresent()) {
                ProductVariant variant = variantOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("variant_id", variant.getVariantId());
                response.put("color", variant.getColor());
                response.put("size", variant.getSize());
                response.put("stock_quantity", variant.getStockQuantity());
                if (variant.getProduct() != null) {
                    response.put("product_name", variant.getProduct().getProductName());
                }
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "Variant not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductVariant>> getVariantsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(variantRepository.findByProductId(productId));
    }
}
