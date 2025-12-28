package com.victusstore.controller;

import com.victusstore.config.JwtUtil;
import com.victusstore.model.Account;
import com.victusstore.model.RefreshToken;
import com.victusstore.model.Seller;
import com.victusstore.repository.AccountRepository;
import com.victusstore.repository.SellerRepository;
import com.victusstore.service.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

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

            Boolean sellerAccount = (Boolean) request.getOrDefault("seller_account", false);
            account.setSellerAccount(sellerAccount);
            account.setRole(sellerAccount ? "SELLER" : "CUSTOMER");
            account.setCreatedAt(LocalDateTime.now());
            account.setIsActive(true);

            Account saved = accountRepository.save(account);

            Long sellerId = null;
            if (sellerAccount) {
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

            // Generate access token and refresh token
            String accessToken = jwtUtil.generateAccessToken(saved.getEmail(), saved.getRole());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(saved.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Account created successfully");
            response.put("email", saved.getEmail());
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken.getToken());
            response.put("role", saved.getRole());
            if (sellerId != null) {
                response.put("seller_id", sellerId);
            }

            logger.info("Registration successful for email: {}", email);
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

            // Determine role
            String role = account.getRole() != null ? account.getRole() : 
                    (account.getSellerAccount() ? "SELLER" : "CUSTOMER");

            // Generate access token and refresh token
            String accessToken = jwtUtil.generateAccessToken(account.getEmail(), role);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(account.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("access_token", accessToken);
            response.put("refresh_token", refreshToken.getToken());
            response.put("email", account.getEmail());
            response.put("role", role);
            response.put("seller_account", account.getSellerAccount());

            logger.info("Login successful for email: {}", email);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refresh_token");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is required"));
            }

            RefreshToken token = refreshTokenService.verifyRefreshToken(refreshToken);
            Account account = accountRepository.findByEmail(token.getUserEmail())
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            if (!account.getIsActive()) {
                return ResponseEntity.status(401).body(Map.of("error", "Account is deactivated"));
            }

            // Create new refresh token first (for replacement tracking)
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(account.getEmail());
            
            // Revoke old token and mark it as replaced (token rotation)
            refreshTokenService.revokeAndReplaceToken(refreshToken, newRefreshToken.getToken());

            String role = account.getRole() != null ? account.getRole() : 
                    (account.getSellerAccount() ? "SELLER" : "CUSTOMER");
            String accessToken = jwtUtil.generateAccessToken(account.getEmail(), role);

            Map<String, Object> response = new HashMap<>();
            response.put("access_token", accessToken);
            response.put("refresh_token", newRefreshToken.getToken());
            response.put("email", account.getEmail());
            response.put("role", role);

            logger.info("Token refreshed for email: {}", account.getEmail());
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            logger.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Token refresh error: {}", e.getMessage(), e);
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
                    "is_seller", account.getSellerAccount(),
                    "role", account.getRole() != null ? account.getRole() : "CUSTOMER"
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
