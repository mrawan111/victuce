package com.victusstore.controller;

import com.victusstore.dto.StoreSettingsResponse;
import com.victusstore.model.StoreSettings;
import com.victusstore.repository.StoreSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class StoreSettingsController {

    @Autowired
    private StoreSettingsRepository storeSettingsRepository;

    @GetMapping
    public ResponseEntity<StoreSettingsResponse> getPublicStoreSettings() {
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

        return ResponseEntity.ok(new StoreSettingsResponse(
                settings.getStoreName(),
                settings.getStoreEmail(),
                settings.getStorePhone(),
                settings.getStoreAddress(),
                settings.getUpdatedAt(),
                settings.getUpdatedBy()
        ));
    }
}
