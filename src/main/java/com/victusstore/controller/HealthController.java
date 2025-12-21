package com.victusstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private static final Instant START_TIME = Instant.now();

    @Autowired(required = false)
    private DataSource dataSource;

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    @Value("${app.version:unknown}")
    private String appVersion;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("application", applicationName);
        health.put("version", appVersion);
        
        // Calculate uptime
        Duration uptime = Duration.between(START_TIME, Instant.now());
        health.put("uptime", formatUptime(uptime));
        health.put("uptime_seconds", uptime.getSeconds());
        
        // Database connectivity check
        Map<String, Object> db = new HashMap<>();
        try {
            if (dataSource != null) {
                try (Connection connection = dataSource.getConnection()) {
                    boolean isValid = connection.isValid(2); // 2 second timeout
                    db.put("status", isValid ? "UP" : "DOWN");
                    db.put("connected", isValid);
                }
            } else {
                db.put("status", "UNKNOWN");
                db.put("connected", false);
            }
        } catch (Exception e) {
            logger.error("Database health check failed: {}", e.getMessage());
            db.put("status", "DOWN");
            db.put("connected", false);
            db.put("error", e.getMessage());
        }
        health.put("database", db);
        
        return ResponseEntity.ok(health);
    }

    private String formatUptime(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        
        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
}

