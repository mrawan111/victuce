package com.victusstore.service;

import com.victusstore.config.JwtUtil;
import com.victusstore.model.RefreshToken;
import com.victusstore.repository.RefreshTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private static final Logger logger = LoggerFactory.getLogger(RefreshTokenService.class);

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.refresh-token.expiration-days:7}")
    private int expirationDays;

    @Transactional
    public RefreshToken createRefreshToken(String userEmail) {
        // Revoke all existing tokens for this user (token rotation)
        refreshTokenRepository.revokeAllUserTokens(userEmail);

        String token = jwtUtil.generateRefreshToken(userEmail);
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userEmail(userEmail)
                .expiresAt(LocalDateTime.now().plusDays(expirationDays))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token has expired");
        }

        if (refreshToken.getRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        return refreshToken;
    }

    @Transactional
    public RefreshToken revokeAndReplaceToken(String oldToken, String newToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(oldToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        
        refreshToken.setRevoked(true);
        refreshToken.setReplacedByToken(newToken);
        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        });
    }

    @Transactional
    public void deleteExpiredTokens() {
        int deleted = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        logger.info("Deleted {} expired refresh tokens", deleted);
    }
}

