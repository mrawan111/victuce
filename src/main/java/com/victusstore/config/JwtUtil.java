package com.victusstore.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // 15 minutes in milliseconds
    private static final int ACCESS_TOKEN_EXPIRATION = 900000;
    // 7 days in milliseconds
    private static final int REFRESH_TOKEN_EXPIRATION = 604800000;

    @Value("${app.jwt.secret:}")
    private String secretKey;

    private Key getSigningKey() {
        String secret = secretKey;
        if (secret == null || secret.trim().isEmpty()) {
            // For production, this should never happen - always set APP_JWT_SECRET
            // For development, generate a secure default
            secret = "dev-secret-key-change-in-production-" + 
                    java.util.UUID.randomUUID().toString().replace("-", "");
            logger.warn("JWT secret not configured, using development default. Set APP_JWT_SECRET environment variable for production.");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, email, ACCESS_TOKEN_EXPIRATION);
    }

    public String generateRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, email, REFRESH_TOKEN_EXPIRATION);
    }

    // Backward compatibility
    public String generateToken(String email, boolean isSeller) {
        String role = isSeller ? "SELLER" : "CUSTOMER";
        return generateAccessToken(email, role);
    }

    private String createToken(Map<String, Object> claims, String subject, int expirationMs) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean validateToken(String token, String email) {
        final String extractedEmail = extractEmail(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }

    public Boolean extractIsSeller(String token) {
        String role = extractRole(token);
        return "SELLER".equals(role) || "ADMIN".equals(role);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> {
            String role = claims.get("role", String.class);
            return role != null ? role : "CUSTOMER";
        });
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractClaim(token, claims -> claims.get("type", String.class)));
    }
}
