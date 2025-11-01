package com.victusstore.controller;

import com.victusstore.model.AdminActivity;
import com.victusstore.repository.AdminActivityRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/activities")
@CrossOrigin(origins = "*")
public class AdminActivityController {

    @Autowired
    private AdminActivityRepository activityRepository;

    @GetMapping
    public ResponseEntity<Page<AdminActivity>> getAllActivities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminActivity> activities = activityRepository.findAllByOrderByCreatedAtDesc(pageable);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getActivityById(@PathVariable Long id) {
        return activityRepository.findById(id)
                .<ResponseEntity<?>>map(activity -> ResponseEntity.ok(activity))
                .orElse(ResponseEntity.status(404).body(Map.of("message", "Activity not found")));
    }

    @GetMapping("/admin/{email}")
    public ResponseEntity<?> getActivitiesByAdmin(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AdminActivity> activities = activityRepository.findByAdminEmailOrderByCreatedAtDesc(email, pageable);
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/entity/{entityType}")
    public ResponseEntity<List<AdminActivity>> getActivitiesByEntityType(@PathVariable String entityType) {
        List<AdminActivity> activities = activityRepository.findByEntityType(entityType.toUpperCase());
        return ResponseEntity.ok(activities);
    }

    @GetMapping("/action/{actionType}")
    public ResponseEntity<List<AdminActivity>> getActivitiesByActionType(@PathVariable String actionType) {
        List<AdminActivity> activities = activityRepository.findByActionType(actionType.toUpperCase());
        return ResponseEntity.ok(activities);
    }

    @PostMapping
    public ResponseEntity<?> logActivity(@RequestBody AdminActivity activity, HttpServletRequest request) {
        try {
            if (activity.getAdminEmail() == null || activity.getAdminEmail().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Admin email is required"));
            }
            if (activity.getActionType() == null || activity.getActionType().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Action type is required"));
            }
            if (activity.getEntityType() == null || activity.getEntityType().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Entity type is required"));
            }

            // Extract IP address and user agent from request
            String ipAddress = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");

            activity.setIpAddress(ipAddress);
            activity.setUserAgent(userAgent);
            activity.setCreatedAt(LocalDateTime.now());

            AdminActivity savedActivity = activityRepository.save(activity);
            return ResponseEntity.status(201).body(savedActivity);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/quick-log")
    public ResponseEntity<?> quickLogActivity(
            @RequestBody Map<String, Object> data,
            HttpServletRequest request) {
        try {
            String adminEmail = (String) data.get("admin_email");
            String actionType = (String) data.get("action_type");
            String entityType = (String) data.get("entity_type");
            String description = (String) data.getOrDefault("description", "");
            Long entityId = data.get("entity_id") != null ? Long.valueOf(data.get("entity_id").toString()) : null;

            if (adminEmail == null || adminEmail.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Admin email is required"));
            }
            if (actionType == null || actionType.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Action type is required"));
            }
            if (entityType == null || entityType.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Entity type is required"));
            }

            AdminActivity activity = AdminActivity.builder()
                    .adminEmail(adminEmail)
                    .actionType(actionType.toUpperCase())
                    .entityType(entityType.toUpperCase())
                    .entityId(entityId)
                    .description(description)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .createdAt(LocalDateTime.now())
                    .build();

            AdminActivity savedActivity = activityRepository.save(activity);
            return ResponseEntity.status(201).body(savedActivity);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        try {
            if (activityRepository.existsById(id)) {
                activityRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("message", "Activity deleted successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "Activity not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // Handle multiple IPs from X-Forwarded-For
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}

