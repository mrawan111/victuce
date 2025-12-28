package com.victusstore.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victusstore.config.JwtUtil;
import com.victusstore.model.Account;
import com.victusstore.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class AdminSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Account adminAccount;
    private Account customerAccount;

    @BeforeEach
    void setUp() {
        // Create admin account
        adminAccount = Account.builder()
                .email("admin@example.com")
                .password(passwordEncoder.encode("password123"))
                .role("ADMIN")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        adminAccount = accountRepository.save(adminAccount);

        // Create customer account
        customerAccount = Account.builder()
                .email("customer@example.com")
                .password(passwordEncoder.encode("password123"))
                .role("CUSTOMER")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        customerAccount = accountRepository.save(customerAccount);
    }

    @Test
    void testAdminEndpoint_AdminCanAccess() throws Exception {
        String adminToken = jwtUtil.generateAccessToken(adminAccount.getEmail(), "ADMIN");

        mockMvc.perform(get("/api/admin/coupons")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void testAdminEndpoint_CustomerCannotAccess() throws Exception {
        String customerToken = jwtUtil.generateAccessToken(customerAccount.getEmail(), "CUSTOMER");

        mockMvc.perform(get("/api/admin/coupons")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("ACCESS_DENIED"));
    }

    @Test
    void testAdminEndpoint_NoTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/coupons"))
                .andExpect(status().isUnauthorized());
    }
}

