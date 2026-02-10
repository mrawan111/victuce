package com.victusstore.controller;

import com.victusstore.repository.ProductVariantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private static final int LOW_STOCK_THRESHOLD = 10;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @GetMapping("/stock-summary")
    public ResponseEntity<List<Map<String, Object>>> getStockSummary() {
        List<Object[]> rows = productVariantRepository.findTotalStockPerProduct();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rows) {
            Long productId = (Long) row[0];
            Number totalStockNumber = (Number) row[1];
            int totalStock = totalStockNumber != null ? totalStockNumber.intValue() : 0;

            Map<String, Object> item = new HashMap<>();
            item.put("productId", productId);
            item.put("totalStock", totalStock);
            item.put("lowStock", totalStock < LOW_STOCK_THRESHOLD);

            result.add(item);
        }

        return ResponseEntity.ok(result);
    }
}

