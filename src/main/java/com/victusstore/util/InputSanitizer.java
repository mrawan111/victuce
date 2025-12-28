package com.victusstore.util;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.regex.Pattern;

/**
 * Input sanitizer that prefers validation + encoding over aggressive removal.
 * Only removes clearly malicious patterns while preserving valid user data.
 */
@Component
public class InputSanitizer {

    // Only match SQL injection attempts in context (e.g., '; DROP TABLE)
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(;\\s*(drop|delete|truncate|alter|create)\\s+)|(union\\s+select)|(exec\\s*\\()|(execute\\s*\\()"
    );

    // Match script tags and event handlers (more specific XSS patterns)
    private static final Pattern XSS_PATTERN = Pattern.compile(
            "(?i)(<script[^>]*>.*?</script>)|(javascript:)|(on\\w+\\s*=)"
    );

    /**
     * Sanitize input by removing only clearly malicious patterns.
     * For HTML output, use HtmlUtils.htmlEscape() instead.
     * This method preserves valid data like "select" in product names.
     */
    public String sanitize(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String sanitized = input;
        
        // Only remove SQL injection patterns in dangerous context
        sanitized = SQL_INJECTION_PATTERN.matcher(sanitized).replaceAll("");
        
        // Remove script tags and event handlers
        sanitized = XSS_PATTERN.matcher(sanitized).replaceAll("");
        
        // Trim whitespace
        sanitized = sanitized.trim();
        
        return sanitized;
    }

    /**
     * HTML escape for safe display in HTML contexts.
     * Prefer this over sanitize() for user-generated content displayed in HTML.
     */
    public String htmlEscape(String input) {
        if (input == null) {
            return null;
        }
        return HtmlUtils.htmlEscape(input.trim());
    }

    public String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    public String sanitizePhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        // Remove all non-digit characters
        String cleaned = phone.replaceAll("[^0-9]", "");
        // If more than 10 digits, take the last 10
        if (cleaned.length() > 10) {
            cleaned = cleaned.substring(cleaned.length() - 10);
        }
        return cleaned;
    }
}

