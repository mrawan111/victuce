    package com.victusstore.controller;

import com.victusstore.model.Seller;
import com.victusstore.repository.AccountRepository;
import com.victusstore.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private AccountRepository accountRepository;

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
    public ResponseEntity<?> createSeller(@RequestBody Seller seller) {
        String email = seller.getEmail() != null ? seller.getEmail().trim().toLowerCase() : null;
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Seller email is required"));
        }

        if (seller.getSellerName() == null || seller.getSellerName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Seller name is required"));
        }

        if (accountRepository.findByEmail(email).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Cannot create seller because no account exists for email " + email
            ));
        }

        List<Seller> existingSellers = sellerRepository.findByEmail(email);
        if (!existingSellers.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "A seller with this email already exists"
            ));
        }

        seller.setEmail(email);
        seller.setSellerName(seller.getSellerName().trim());
        if (seller.getRating() == null) {
            seller.setRating(BigDecimal.ZERO);
        }
        if (seller.getIsActive() == null) {
            seller.setIsActive(true);
        }

        Seller savedSeller = sellerRepository.save(seller);
        return ResponseEntity.ok(savedSeller);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSeller(@PathVariable Long id, @RequestBody Seller sellerDetails) {
        return sellerRepository.findById(id)
                .map(seller -> {
                    if (sellerDetails.getSellerName() != null && !sellerDetails.getSellerName().trim().isEmpty()) {
                        seller.setSellerName(sellerDetails.getSellerName().trim());
                    }
                    if (sellerDetails.getEmail() != null && !sellerDetails.getEmail().trim().isEmpty()) {
                        String normalizedEmail = sellerDetails.getEmail().trim().toLowerCase();
                        if (accountRepository.findByEmail(normalizedEmail).isEmpty()) {
                            return ResponseEntity.badRequest().body(Map.of(
                                    "error", "Cannot assign seller email because no account exists for email " + normalizedEmail
                            ));
                        }

                        List<Seller> existingSellers = sellerRepository.findByEmail(normalizedEmail);
                        Optional<Seller> conflictingSeller = existingSellers.stream()
                                .filter(existing -> !existing.getSellerId().equals(id))
                                .findFirst();
                        if (conflictingSeller.isPresent()) {
                            return ResponseEntity.badRequest().body(Map.of(
                                    "error", "Another seller already uses this email"
                            ));
                        }

                        seller.setEmail(normalizedEmail);
                    }
                    if (sellerDetails.getRating() != null) {
                        seller.setRating(sellerDetails.getRating());
                    }
                    if (sellerDetails.getIsActive() != null) {
                        seller.setIsActive(sellerDetails.getIsActive());
                    }
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
