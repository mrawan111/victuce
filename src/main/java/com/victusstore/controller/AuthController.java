package com.victusstore.controller;

import com.victusstore.config.JwtUtil;
import com.victusstore.model.Account;
import com.victusstore.model.Seller;
import com.victusstore.repository.AccountRepository;
import com.victusstore.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> request) {
        try {
            String email = (String) request.get("email");

            if (accountRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            Account account = new Account();
            account.setEmail(email);
            account.setPassword(passwordEncoder.encode((String) request.get("password")));
            account.setFirstName((String) request.get("first_name"));
            account.setLastName((String) request.get("last_name"));
            account.setPhoneNum((String) request.get("phone_num"));
            account.setSellerAccount((Boolean) request.getOrDefault("seller_account", false));
            account.setCreatedAt(LocalDateTime.now());
            account.setIsActive(true);

            Account saved = accountRepository.save(account);

            Long sellerId = null;
            if (account.getSellerAccount()) {
                Seller seller = new Seller();
                seller.setEmail(email);
                seller.setSellerName(account.getFirstName() + " " + account.getLastName());
                Seller savedSeller = sellerRepository.save(seller);
                sellerId = savedSeller.getSellerId();
            }

            String token = jwtUtil.generateToken(saved.getEmail(), account.getSellerAccount());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account created successfully");
            response.put("email", saved.getEmail());
            response.put("token", token);
            if (sellerId != null) {
                response.put("seller_id", sellerId);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            if (!passwordEncoder.matches(password, account.getPassword())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid credentials"));
            }

            account.setLastLogin(LocalDateTime.now());
            accountRepository.save(account);

            String token = jwtUtil.generateToken(account.getEmail(), account.getSellerAccount());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("email", account.getEmail());
            response.put("seller_account", account.getSellerAccount());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/check_account/{email}/{password}")
    public ResponseEntity<?> checkAccount(@PathVariable String email, @PathVariable String password) {
        try {
            Account account = accountRepository.findByEmail(email).orElse(null);

            if (account == null) {
                return ResponseEntity.status(404).body(Map.of("exists", false));
            }

            boolean passwordMatches = passwordEncoder.matches(password, account.getPassword());

            if (passwordMatches) {
                return ResponseEntity.ok(Map.of(
                    "password", true,
                    "exists", true,
                    "is_seller", account.getSellerAccount()
                ));
            } else {
                return ResponseEntity.status(401).body(Map.of(
                    "password", false,
                    "exists", true
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
