package com.victusstore.service;

import com.victusstore.repository.IdempotencyKeyRepository;
import com.victusstore.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CleanupService {

    private static final Logger logger = LoggerFactory.getLogger(CleanupService.class);

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    /**
     * Cleanup expired idempotency keys and refresh tokens.
     * Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredKeys() {
        LocalDateTime now = LocalDateTime.now();
        
        try {
            int deletedIdempotencyKeys = idempotencyKeyRepository.deleteExpiredKeys(now);
            int deletedRefreshTokens = refreshTokenRepository.deleteExpiredTokens(now);
            
            logger.info("Cleanup completed: {} expired idempotency keys, {} expired refresh tokens deleted", 
                    deletedIdempotencyKeys, deletedRefreshTokens);
        } catch (Exception e) {
            logger.error("Error during cleanup: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual cleanup method for testing or on-demand cleanup.
     */
    @Transactional
    public CleanupResult cleanupNow() {
        LocalDateTime now = LocalDateTime.now();
        int deletedIdempotencyKeys = idempotencyKeyRepository.deleteExpiredKeys(now);
        int deletedRefreshTokens = refreshTokenRepository.deleteExpiredTokens(now);
        
        return new CleanupResult(deletedIdempotencyKeys, deletedRefreshTokens);
    }

    public record CleanupResult(int deletedIdempotencyKeys, int deletedRefreshTokens) {}
}

