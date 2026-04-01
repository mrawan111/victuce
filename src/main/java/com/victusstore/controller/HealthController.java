package com.victusstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Value("${app.version:0.0.1}")
    private String appVersion;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        boolean dbConnected = isDatabaseConnected();
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        health.put("status", dbConnected ? "healthy" : "unhealthy");
        health.put("timestamp", java.time.Instant.now().toString());
        health.put("version", appVersion);
        health.put("uptime", uptimeSeconds);
        health.put("db", dbConnected ? "connected" : "disconnected");

        return ResponseEntity.status(dbConnected ? 200 : 503).body(health);
    }

    private boolean isDatabaseConnected() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}

