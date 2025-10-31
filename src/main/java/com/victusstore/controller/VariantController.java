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
@CrossOrigin(origins = "*")
public class VariantController {

    @Autowired
    private ProductVariantRepository variantRepository;

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
