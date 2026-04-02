-- Create regions table
CREATE TABLE IF NOT EXISTS regions (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    region_code VARCHAR(10) NOT NULL UNIQUE
);

-- Create cities table
CREATE TABLE IF NOT EXISTS cities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    region_code VARCHAR(10) NOT NULL,
    CONSTRAINT fk_cities_region 
        FOREIGN KEY(region_code) 
        REFERENCES regions(region_code)
        ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_cities_region_code ON cities(region_code);
CREATE INDEX IF NOT EXISTS idx_regions_region_code ON regions(region_code);

-- Add comments for documentation
COMMENT ON TABLE regions IS 'Egyptian governorates/regions';
COMMENT ON TABLE cities IS 'Egyptian cities linked to regions';
COMMENT ON COLUMN regions.region_code IS 'ISO code for the region (unique)';
COMMENT ON COLUMN cities.region_code IS 'Foreign key reference to regions.region_code';
