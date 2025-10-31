    package com.victusstore.controller;

import com.victusstore.model.Seller;
import com.victusstore.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    @Autowired
    private SellerRepository sellerRepository;

    @GetMapping
    public ResponseEntity<List<Seller>> getAllSellers() {
        List<Seller> sellers = sellerRepository.findAll();
        return ResponseEntity.ok(sellers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seller> getSellerById(@PathVariable Long id) {
        return sellerRepository.findById(id)
                .map(seller -> ResponseEntity.ok(seller))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Seller> createSeller(@RequestBody Seller seller) {
        Seller savedSeller = sellerRepository.save(seller);
        return ResponseEntity.ok(savedSeller);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Seller> updateSeller(@PathVariable Long id, @RequestBody Seller sellerDetails) {
        return sellerRepository.findById(id)
                .map(seller -> {
                    seller.setSellerName(sellerDetails.getSellerName());
                    seller.setRating(sellerDetails.getRating());
                    seller.setIsActive(sellerDetails.getIsActive());
                    Seller updatedSeller = sellerRepository.save(seller);
                    return ResponseEntity.ok(updatedSeller);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteSeller(@PathVariable Long id) {
        return sellerRepository.findById(id)
                .map(seller -> {
                    sellerRepository.delete(seller);
                    Map<String, Boolean> response = new HashMap<>();
                    response.put("deleted", Boolean.TRUE);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
