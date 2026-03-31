    package com.victusstore.controller;

import com.victusstore.model.Seller;
import com.victusstore.model.Account;
import com.victusstore.repository.AccountRepository;
import com.victusstore.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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
    public ResponseEntity<?> createSeller(@RequestBody Map<String, Object> payload) {
        String sellerName = payload.get("sellerName") != null ? payload.get("sellerName").toString().trim() : null;
        String email = payload.get("email") != null ? payload.get("email").toString().trim().toLowerCase() : null;
        String password = payload.get("password") != null ? payload.get("password").toString() : null;
        Boolean isActive = payload.get("isActive") != null ? Boolean.valueOf(payload.get("isActive").toString()) : Boolean.TRUE;

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Seller email is required"));
        }

        if (sellerName == null || sellerName.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Seller name is required"));
        }

        Optional<Account> existingAccount = accountRepository.findByEmail(email);
        if (existingAccount.isEmpty()) {
            if (password == null || password.length() < 8) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Password must be at least 8 characters long when creating a seller account"
                ));
            }

            String[] nameParts = sellerName.split("\\s+", 2);
            String firstName = nameParts.length > 0 ? nameParts[0] : sellerName;
            String lastName = nameParts.length > 1 ? nameParts[1] : "";

            Account account = new Account();
            account.setEmail(email);
            account.setPassword(passwordEncoder.encode(password));
            account.setFirstName(firstName);
            account.setLastName(lastName);
            account.setSellerAccount(true);
            account.setRole("SELLER");
            account.setIsActive(isActive);
            account.setCreatedAt(LocalDateTime.now());
            accountRepository.save(account);
        } else {
            Account account = existingAccount.get();
            account.setSellerAccount(true);
            account.setRole("SELLER");
            if (account.getIsActive() == null) {
                account.setIsActive(Boolean.TRUE);
            }
            accountRepository.save(account);
        }

        List<Seller> existingSellers = sellerRepository.findByEmail(email);
        if (!existingSellers.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "A seller with this email already exists"
            ));
        }

        Seller seller = new Seller();
        seller.setEmail(email);
        seller.setSellerName(sellerName);
        if (payload.get("rating") != null && !payload.get("rating").toString().trim().isEmpty()) {
            seller.setRating(new BigDecimal(payload.get("rating").toString()));
        } else {
            seller.setRating(BigDecimal.ZERO);
        }
        seller.setIsActive(isActive);

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
