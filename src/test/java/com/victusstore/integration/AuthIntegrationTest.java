package com.victusstore.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victusstore.model.Account;
import com.victusstore.model.RefreshToken;
import com.victusstore.repository.AccountRepository;
import com.victusstore.repository.RefreshTokenRepository;
import com.victusstore.config.JwtUtil;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        testAccount = Account.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .role("CUSTOMER")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        testAccount = accountRepository.save(testAccount);
    }

    @Test
    void testRefreshFlow_ExpiredAccessTokenCanRefresh() throws Exception {
        // Login to get tokens
        Map<String, String> loginData = new HashMap<>();
        loginData.put("email", "test@example.com");
        loginData.put("password", "password123");

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginData)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> loginResult = objectMapper.readValue(loginResponse, Map.class);
        String refreshToken = (String) loginResult.get("refresh_token");
        assertNotNull(refreshToken, "Refresh token should be returned");

        // Verify refresh token exists in database
        RefreshToken tokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AssertionError("Refresh token should be stored"));
        assertFalse(tokenEntity.getRevoked(), "Refresh token should not be revoked");
        assertTrue(tokenEntity.isActive(), "Refresh token should be active");

        // Refresh access token
        Map<String, String> refreshData = new HashMap<>();
        refreshData.put("refresh_token", refreshToken);

        String refreshResponse = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshData)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> refreshResult = objectMapper.readValue(refreshResponse, Map.class);
        String newAccessToken = (String) refreshResult.get("access_token");
        String newRefreshToken = (String) refreshResult.get("refresh_token");

        assertNotNull(newAccessToken, "New access token should be returned");
        assertNotNull(newRefreshToken, "New refresh token should be returned");
        assertNotEquals(refreshToken, newRefreshToken, "New refresh token should be different");

        // Verify old token is revoked and marked as replaced
        RefreshToken oldToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AssertionError("Old token should still exist"));
        assertTrue(oldToken.getRevoked(), "Old refresh token should be revoked");
        assertEquals(newRefreshToken, oldToken.getReplacedByToken(), "Old token should reference new token");

        // Verify new token is active
        RefreshToken newTokenEntity = refreshTokenRepository.findByToken(newRefreshToken)
                .orElseThrow(() -> new AssertionError("New refresh token should be stored"));
        assertFalse(newTokenEntity.getRevoked(), "New refresh token should not be revoked");
        assertTrue(newTokenEntity.isActive(), "New refresh token should be active");
    }

    @Test
    void testRefreshFlow_RevokedTokenCannotRefresh() throws Exception {
        // Create a revoked refresh token
        RefreshToken revokedToken = RefreshToken.builder()
                .token("revoked-token")
                .userEmail(testAccount.getEmail())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .createdAt(LocalDateTime.now())
                .revoked(true)
                .build();
        refreshTokenRepository.save(revokedToken);

        Map<String, String> refreshData = new HashMap<>();
        refreshData.put("refresh_token", "revoked-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshData)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").exists());
    }
}

