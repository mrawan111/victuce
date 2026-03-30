package com.victusstore.dto;

import java.time.LocalDateTime;

public record StoreSettingsResponse(
        String storeName,
        String storeEmail,
        String storePhone,
        String storeAddress,
        LocalDateTime updatedAt,
        String updatedBy
) {
}
