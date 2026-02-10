package com.victusstore.controller;

import com.victusstore.model.StoreSettings;
import com.victusstore.repository.StoreSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    @Autowired
    private StoreSettingsRepository storeSettingsRepository;

    @GetMapping
    public ResponseEntity<?> getStoreSettings() {
        StoreSettings settings = storeSettingsRepository.findTopByOrderByIdAsc()
                .orElse(null);

        if (settings == null) {
            // No settings saved yet – return empty/default payload
            Map<String, Object> response = new HashMap<>();
            response.put("storeName", null);
            response.put("storeEmail", null);
            response.put("storePhone", null);
            response.put("storeAddress", null);
            response.put("updatedAt", null);
            response.put("updatedBy", null);
            return ResponseEntity.ok(response);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("storeName", settings.getStoreName());
        response.put("storeEmail", settings.getStoreEmail());
        response.put("storePhone", settings.getStorePhone());
        response.put("storeAddress", settings.getStoreAddress());
        response.put("updatedAt", settings.getUpdatedAt());
        response.put("updatedBy", settings.getUpdatedBy());

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<?> updateStoreSettings(@RequestBody Map<String, Object> body) {
        try {
            String storeName = body.get("storeName") != null ? body.get("storeName").toString().trim() : null;
            String storeEmail = body.get("storeEmail") != null ? body.get("storeEmail").toString().trim() : null;
            String storePhone = body.get("storePhone") != null ? body.get("storePhone").toString().trim() : null;
            String storeAddress = body.get("storeAddress") != null ? body.get("storeAddress").toString().trim() : null;

            if (storeName == null || storeName.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "storeName is required"));
            }
            if (storeEmail == null || storeEmail.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "storeEmail is required"));
            }
            if (!storeEmail.contains("@") || !storeEmail.contains(".")) {
                return ResponseEntity.badRequest().body(Map.of("error", "storeEmail is invalid"));
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String updatedBy = auth != null ? auth.getName() : null;

            StoreSettings settings = storeSettingsRepository.findTopByOrderByIdAsc()
                    .orElseGet(StoreSettings::new);

            settings.setStoreName(storeName);
            settings.setStoreEmail(storeEmail);
            settings.setStorePhone(storePhone);
            settings.setStoreAddress(storeAddress);
            settings.setUpdatedAt(LocalDateTime.now());
            settings.setUpdatedBy(updatedBy);

            StoreSettings saved = storeSettingsRepository.save(settings);

            Map<String, Object> response = new HashMap<>();
            response.put("storeName", saved.getStoreName());
            response.put("storeEmail", saved.getStoreEmail());
            response.put("storePhone", saved.getStorePhone());
            response.put("storeAddress", saved.getStoreAddress());
            response.put("updatedAt", saved.getUpdatedAt());
            response.put("updatedBy", saved.getUpdatedBy());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
}

