-- Migration to add Egypt region and city fields to orders table
-- This script adds region and city columns for Egypt-specific checkout implementation

BEGIN;

-- Add region column (governorate/administrative area)
ALTER TABLE IF EXISTS public.orders
    ADD COLUMN IF NOT EXISTS region character varying(100) COLLATE pg_catalog."default";

-- Add city column (city/locality)
ALTER TABLE IF EXISTS public.orders
    ADD COLUMN IF NOT EXISTS city character varying(100) COLLATE pg_catalog."default";

-- Add comments for documentation
COMMENT ON COLUMN public.orders.region IS 'Egypt governorate/region (e.g., Cairo, Giza, Alexandria)';
COMMENT ON COLUMN public.orders.city IS 'City/locality within the region (e.g., New Cairo, Helwan)';

-- Create index on region and city for faster queries
CREATE INDEX IF NOT EXISTS idx_orders_region ON public.orders USING btree (region);
CREATE INDEX IF NOT EXISTS idx_orders_city ON public.orders USING btree (city);
CREATE INDEX IF NOT EXISTS idx_orders_region_city ON public.orders USING btree (region, city);

COMMIT;
