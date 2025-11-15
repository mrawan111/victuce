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
            // Validate required fields
            String email = (String) request.get("email");
            String password = (String) request.get("password");

            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (password == null || password.length() < 8) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 8 characters long"));
            }

            email = email.trim().toLowerCase();

            if (accountRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
            }

            Account account = new Account();
            account.setEmail(email);
            account.setPassword(passwordEncoder.encode(password));
            account.setFirstName((String) request.get("first_name"));
            account.setLastName((String) request.get("last_name"));

            // Clean phone number - extract only digits and ensure exactly 10 digits
            String phoneNum = (String) request.get("phone_num");
            if (phoneNum != null && !phoneNum.trim().isEmpty()) {
                // Remove all non-digit characters
                String cleanPhone = phoneNum.replaceAll("[^0-9]", "");
                // If more than 10 digits, take the last 10 (local number)
                if (cleanPhone.length() > 10) {
                    cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
                }
                // If less than 10 digits, pad with zeros or reject
                if (cleanPhone.length() < 10) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Phone number must contain at least 10 digits"));
                }
                account.setPhoneNum(cleanPhone);
            }

            account.setSellerAccount((Boolean) request.getOrDefault("seller_account", false));
            account.setCreatedAt(LocalDateTime.now());
            account.setIsActive(true);

            Account saved = accountRepository.save(account);

            Long sellerId = null;
            if (account.getSellerAccount()) {
                Seller seller = new Seller();
                seller.setEmail(email);
                String firstName = account.getFirstName() != null ? account.getFirstName() : "";
                String lastName = account.getLastName() != null ? account.getLastName() : "";
                seller.setSellerName((firstName + " " + lastName).trim());
                if (seller.getSellerName().isEmpty()) {
                    seller.setSellerName("Seller");
                }
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

            System.out.println("Registration successful for email: " + email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            // Validate request body
            if (credentials == null || !credentials.containsKey("email") || !credentials.containsKey("password")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email and password are required"));
            }

            String email = credentials.get("email").trim();
            String password = credentials.get("password");

            // Validate email format
            if (email.isEmpty() || !email.contains("@")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email format"));
            }

            // Check if account exists
            Account account = accountRepository.findByEmail(email).orElse(null);
            if (account == null) {
                System.out.println("Login attempt failed: Account not found for email: " + email);
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
            }

            // Check if account is active
            if (!account.getIsActive()) {
                System.out.println("Login attempt failed: Account inactive for email: " + email);
                return ResponseEntity.status(401).body(Map.of("error", "Account is deactivated"));
            }

            // Verify password
            if (!passwordEncoder.matches(password, account.getPassword())) {
                System.out.println("Login attempt failed: Invalid password for email: " + email);
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
            }

            // Update last login
            account.setLastLogin(LocalDateTime.now());
            accountRepository.save(account);

            // Generate token
            String token = jwtUtil.generateToken(account.getEmail(), account.getSellerAccount());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);
            response.put("email", account.getEmail());
            response.put("seller_account", account.getSellerAccount());

            System.out.println("Login successful for email: " + email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
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
