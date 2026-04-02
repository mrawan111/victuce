-- Egypt Regions and Cities Database Schema
-- This script creates tables for managing Egypt regions and cities with proper constraints

-- Drop existing tables if they exist (for clean recreation)
DROP TABLE IF EXISTS cities;
DROP TABLE IF EXISTS regions;

-- Create regions table
CREATE TABLE regions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    region_code VARCHAR(10) UNIQUE NOT NULL
);

-- Create cities table with foreign key relationship
CREATE TABLE cities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    region_code VARCHAR(10) NOT NULL,
    is_other BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (region_code) REFERENCES regions(region_code) ON DELETE CASCADE,
    UNIQUE (name, region_code) -- Ensures no duplicate cities within the same region
);

-- Create indexes for better query performance
CREATE INDEX idx_regions_code ON regions(region_code);
CREATE INDEX idx_cities_region_code ON cities(region_code);
CREATE INDEX idx_cities_name ON cities(name);

-- Add comments for documentation
COMMENT ON TABLE regions IS 'Egypt regions with unique codes';
COMMENT ON TABLE cities IS 'Egypt cities with region association and Other option support';
COMMENT ON COLUMN regions.region_code IS 'Unique code for each region (e.g., CAI, ALX)';
COMMENT ON COLUMN cities.is_other IS 'Flag to identify "Other" city options for custom entries';
