-- Flyway Migration: V3__Add_Store_Settings
-- Description: Adds persisted store settings for the admin settings page

CREATE TABLE IF NOT EXISTS public.store_settings
(
    id bigserial PRIMARY KEY,
    store_name VARCHAR(255) NOT NULL,
    store_email VARCHAR(255) NOT NULL,
    store_phone VARCHAR(50),
    store_address VARCHAR(500),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_store_settings_updated_at
    ON public.store_settings(updated_at);
