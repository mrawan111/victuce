package com.victusstore.controller;

import com.victusstore.dto.StoreSettingsRequest;
import com.victusstore.dto.StoreSettingsResponse;
import com.victusstore.model.StoreSettings;
import com.victusstore.repository.StoreSettingsRepository;
import jakarta.validation.Valid;
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

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    @Autowired
    private StoreSettingsRepository storeSettingsRepository;

    @GetMapping
    public ResponseEntity<StoreSettingsResponse> getStoreSettings() {
        StoreSettings settings = storeSettingsRepository.findTopByOrderByIdAsc()
                .orElse(null);

        if (settings == null) {
            return ResponseEntity.ok(new StoreSettingsResponse(
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            ));
        }

        return ResponseEntity.ok(toResponse(settings));
    }

    @PutMapping
    public ResponseEntity<StoreSettingsResponse> updateStoreSettings(
            @Valid @RequestBody StoreSettingsRequest body) {
        StoreSettings settings = storeSettingsRepository.findTopByOrderByIdAsc()
                .orElseGet(StoreSettings::new);

        settings.setStoreName(body.storeName().trim());
        settings.setStoreEmail(body.storeEmail().trim());
        settings.setStorePhone(normalizeNullable(body.storePhone()));
        settings.setStoreAddress(normalizeNullable(body.storeAddress()));
        settings.setUpdatedAt(LocalDateTime.now());
        settings.setUpdatedBy(resolveUpdatedBy());

        StoreSettings saved = storeSettingsRepository.save(settings);
        return ResponseEntity.ok(toResponse(saved));
    }

    private StoreSettingsResponse toResponse(StoreSettings settings) {
        return new StoreSettingsResponse(
                settings.getStoreName(),
                settings.getStoreEmail(),
                settings.getStorePhone(),
                settings.getStoreAddress(),
                settings.getUpdatedAt(),
                settings.getUpdatedBy()
        );
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String resolveUpdatedBy() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        String name = auth.getName();
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
            return null;
        }

        return name;
    }
}
