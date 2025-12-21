-- Flyway Migration: V2__Add_Production_Features
-- Description: Adds idempotency keys, refresh tokens, and role support with proper indexes

-- Add role column to accounts table
ALTER TABLE public.accounts 
ADD COLUMN IF NOT EXISTS role VARCHAR(20) DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'SELLER', 'ADMIN'));

-- Update existing seller accounts
UPDATE public.accounts 
SET role = 'SELLER' 
WHERE seller_account = true AND role = 'CUSTOMER';

-- Create idempotency_keys table
CREATE TABLE IF NOT EXISTS public.idempotency_keys
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key VARCHAR(255) NOT NULL,
    user_email VARCHAR(255) NOT NULL,
    endpoint VARCHAR(255) NOT NULL,
    request_hash VARCHAR(64),
    response_body TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    -- Unique constraint on (key, user_email, endpoint) for security
    CONSTRAINT uk_idempotency_key_user_endpoint UNIQUE (key, user_email, endpoint)
);

-- Create indexes for idempotency_keys
CREATE INDEX IF NOT EXISTS idx_idempotency_key ON public.idempotency_keys(key);
CREATE INDEX IF NOT EXISTS idx_idempotency_user_email ON public.idempotency_keys(user_email);
CREATE INDEX IF NOT EXISTS idx_idempotency_expires_at ON public.idempotency_keys(expires_at);

-- Create refresh_tokens table
CREATE TABLE IF NOT EXISTS public.refresh_tokens
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(500) NOT NULL UNIQUE,
    user_email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT false,
    replaced_by_token VARCHAR(500)
);

-- Create indexes for refresh_tokens
CREATE INDEX IF NOT EXISTS idx_refresh_token_token ON public.refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_token_user_email ON public.refresh_tokens(user_email);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expires_at ON public.refresh_tokens(expires_at);

