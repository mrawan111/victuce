package com.victusstore.filter;

import com.victusstore.config.RateLimitingConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(2)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Autowired
    private RateLimitingConfig.RateLimiter rateLimiter;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String clientId = getClientId(request);

        // Rate limit rules
        if (path.startsWith("/api/auth/login")) {
            if (!rateLimiter.allowRequest(clientId + ":login", 5, 60000)) { // 5 requests per minute
                logger.warn("Rate limit exceeded for login: {}", clientId);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many login attempts. Please try again later.\"}}");
                return;
            }
        } else if (path.startsWith("/api/auth/register")) {
            if (!rateLimiter.allowRequest(clientId + ":register", 3, 60000)) { // 3 requests per minute
                logger.warn("Rate limit exceeded for register: {}", clientId);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many registration attempts. Please try again later.\"}}");
                return;
            }
        } else if (path.startsWith("/api/orders/from-cart")) {
            if (!rateLimiter.allowRequest(clientId + ":checkout", 10, 60000)) { // 10 requests per minute
                logger.warn("Rate limit exceeded for checkout: {}", clientId);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many checkout attempts. Please try again later.\"}}");
                return;
            }
        } else if (path.startsWith("/api/admin/coupons/validate")) {
            if (!rateLimiter.allowRequest(clientId + ":validate", 20, 60000)) { // 20 requests per minute
                logger.warn("Rate limit exceeded for coupon validation: {}", clientId);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":{\"code\":\"RATE_LIMIT_EXCEEDED\",\"message\":\"Too many coupon validation attempts. Please try again later.\"}}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientId(HttpServletRequest request) {
        // Try to get client IP
        String ip = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            ip = forwarded.split(",")[0].trim();
        }
        return ip;
    }
}

