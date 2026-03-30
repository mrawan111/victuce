package com.victusstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StoreSettingsRequest(
        @NotBlank(message = "storeName is required")
        @Size(max = 255, message = "storeName must be at most 255 characters")
        String storeName,

        @NotBlank(message = "storeEmail is required")
        @Email(message = "storeEmail must be a valid email address")
        @Size(max = 255, message = "storeEmail must be at most 255 characters")
        String storeEmail,

        @Size(max = 50, message = "storePhone must be at most 50 characters")
        String storePhone,

        @Size(max = 500, message = "storeAddress must be at most 500 characters")
        String storeAddress
) {
}
