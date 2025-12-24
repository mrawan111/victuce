package com.victusstore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victusstore.exception.IdempotencyMismatchException;
import com.victusstore.model.IdempotencyKey;
import com.victusstore.repository.IdempotencyKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class IdempotencyService {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyService.class);
    
    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${app.idempotency.ttl-hours:24}")
    private int ttlHours;

    /**
     * Fetch cached response or enforce mismatch rules under lock.
     * If the key exists and request hash differs, throw IdempotencyMismatchException.
     * If the hash matches and response exists, return it. Expired keys are removed.
     */
    @Transactional
    public Optional<String> getCachedResponseOrThrowOnMismatch(String idempotencyKey, String userEmail, String endpoint, Object requestBody) {
        Optional<IdempotencyKey> existing = idempotencyKeyRepository.findByKeyAndUserEmailForUpdate(idempotencyKey, userEmail);

        if (existing.isEmpty()) {
            return Optional.empty();
        }

        IdempotencyKey key = existing.get();

        // Check if expired
        if (key.getExpiresAt().isBefore(LocalDateTime.now())) {
            logger.debug("Idempotency key expired: {}", idempotencyKey);
            idempotencyKeyRepository.delete(key);
            return Optional.empty();
        }

        // Verify endpoint matches (if not, treat as cache miss)
        if (!key.getEndpoint().equals(endpoint)) {
            logger.warn("Idempotency key endpoint mismatch: {} != {}", key.getEndpoint(), endpoint);
            return Optional.empty();
        }

        // Verify request hash
        String requestHash = computeRequestHash(requestBody);
        if (key.getRequestHash() != null && requestHash != null && !key.getRequestHash().equals(requestHash)) {
            logger.warn("Idempotency key request hash mismatch for key {}", idempotencyKey);
            throw new IdempotencyMismatchException("Idempotency key reuse with different request payload");
        }

        if (key.getResponseBody() != null) {
            logger.info("Returning cached response for idempotency key: {}", idempotencyKey);
            return Optional.of(key.getResponseBody());
        }

        return Optional.empty();
    }

    @Transactional
    public void storeResponse(String idempotencyKey, String userEmail, String endpoint, Object requestBody, Object response) {
        try {
            String responseBody = objectMapper.writeValueAsString(response);
            String requestHash = computeRequestHash(requestBody);
            
            IdempotencyKey key = IdempotencyKey.builder()
                    .key(idempotencyKey)
                    .userEmail(userEmail)
                    .endpoint(endpoint)
                    .requestHash(requestHash)
                    .responseBody(responseBody)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusHours(ttlHours))
                    .build();
            
            idempotencyKeyRepository.save(key);
            logger.debug("Stored idempotency key: {}", idempotencyKey);
        } catch (Exception e) {
            logger.error("Failed to store idempotency key: {}", e.getMessage(), e);
            // Don't throw - idempotency is best-effort
        }
    }

    private String computeRequestHash(Object requestBody) {
        try {
            String json = objectMapper.writeValueAsString(requestBody);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(json.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            logger.warn("Failed to compute request hash: {}", e.getMessage());
            return null;
        }
    }
}

