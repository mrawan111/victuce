-- ============================================================
-- VictusStore Complete Database Creation Script
-- ============================================================
-- This script creates the complete database schema for VictusStore
-- Includes: Core tables, Admin tables, Indexes, and Triggers
--
-- Usage:
-- 1. First run: DROP DATABASE and CREATE DATABASE commands
-- 2. Connect to the database
-- 3. Run all table creation commands below
-- ============================================================

-- Drop and Create Database
DROP DATABASE IF EXISTS vectusse_store;
CREATE DATABASE vectusse_store;

-- Connect to the database (uncomment and run separately if using psql)
-- \c vectusse_store;

-- ============================================================
-- CORE TABLES
-- ============================================================

-- Create Accounts Table with improved constraints and timestamps
CREATE TABLE Accounts (
    email VARCHAR(255) PRIMARY KEY,
	first_name VARCHAR(255),
	last_name VARCHAR(255),
    password VARCHAR(255) NOT NULL CHECK (LENGTH(password) >= 8),
    phone_num VARCHAR(15) CHECK (phone_num ~ '^[0-9]{10}$' OR phone_num IS NULL),
    seller_account BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Sellers Table with improved relations
CREATE TABLE Sellers (
    seller_id SERIAL PRIMARY KEY,
    seller_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    rating DECIMAL(3,2) DEFAULT 0.0 CHECK (rating >= 0 AND rating <= 5.0),
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (email) REFERENCES Accounts(email) ON DELETE CASCADE
);

-- Create Categories Table with hierarchy support
CREATE TABLE Categories (
    category_id SERIAL PRIMARY KEY,
    category_name VARCHAR(255) NOT NULL UNIQUE,
    category_image VARCHAR(255),
    parent_category_id INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_category_id) REFERENCES Categories(category_id)
);

-- Create Products Table with improved constraints
CREATE TABLE Products (
    product_id SERIAL PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    description TEXT,
    base_price DECIMAL(10, 2) NOT NULL CHECK (base_price >= 0),
    category_id INTEGER,
    seller_id INTEGER,
    product_rating DECIMAL(2, 1) DEFAULT 0.0 CHECK (product_rating >= 0 AND product_rating <= 5),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES Categories(category_id) ON DELETE SET NULL,
    FOREIGN KEY (seller_id) REFERENCES Sellers(seller_id) ON DELETE CASCADE
);

-- Create Product_Variants Table
CREATE TABLE Product_Variants (
    variant_id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL,
    color VARCHAR(50) NOT NULL,
    size VARCHAR(50) NOT NULL,
    stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    sku VARCHAR(50) UNIQUE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE
);

-- Create Images Table
CREATE TABLE Images (
    image_id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL,
    variant_id INTEGER,
    image_url VARCHAR(255) NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id) ON DELETE CASCADE
);

-- Create Cart Table
CREATE TABLE Cart (
    cart_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_price DECIMAL(10, 2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (email) REFERENCES Accounts(email) ON DELETE CASCADE
);

-- Create Orders Table
CREATE TABLE Orders (
    order_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    address TEXT NOT NULL,
    phone_num VARCHAR(15) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL CHECK (total_price >= 0),
    order_status VARCHAR(20) NOT NULL DEFAULT 'pending' 
        CHECK (order_status IN ('pending', 'processing', 'shipped', 'delivered', 'cancelled')),
    payment_status VARCHAR(20) NOT NULL DEFAULT 'pending' 
        CHECK (payment_status IN ('pending', 'paid', 'failed', 'refunded')),
    payment_method VARCHAR(50),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (email) REFERENCES Accounts(email) ON DELETE NO ACTION
);

-- Create Cart_Products Table
CREATE TABLE Cart_Products (
    id SERIAL PRIMARY KEY,
    variant_id INTEGER NOT NULL,
    cart_id INTEGER NOT NULL,
    order_id INTEGER,
    quantity INTEGER NOT NULL CHECK (quantity >= 1),
    price_at_time DECIMAL(10,2) NOT NULL CHECK (price_at_time >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (variant_id) REFERENCES Product_Variants(variant_id) ON DELETE NO ACTION,
    FOREIGN KEY (cart_id) REFERENCES Cart(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE SET NULL
);

-- Create Reviews Table
CREATE TABLE Reviews (
    review_id SERIAL PRIMARY KEY,
    product_id INTEGER NOT NULL,
    email VARCHAR(255) NOT NULL,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Products(product_id) ON DELETE CASCADE,
    FOREIGN KEY (email) REFERENCES Accounts(email) ON DELETE CASCADE
);

-- Create Shipping_Tracking Table
CREATE TABLE Shipping_Tracking (
    tracking_id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL,
    carrier_name VARCHAR(100),
    tracking_number VARCHAR(100),
    shipping_status VARCHAR(20) CHECK (shipping_status IN ('pending', 'in_transit', 'delivered', 'failed')),
    estimated_delivery TIMESTAMP,
    actual_delivery TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE
);

-- ============================================================
-- ADMIN TABLES
-- ============================================================

-- Create Coupons Table
CREATE TABLE Coupons (
    coupon_id SERIAL PRIMARY KEY,
    coupon_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    discount_type VARCHAR(20) NOT NULL CHECK (discount_type IN ('PERCENTAGE', 'FIXED')),
    discount_value DECIMAL(10, 2) NOT NULL CHECK (discount_value >= 0),
    min_purchase_amount DECIMAL(10, 2) CHECK (min_purchase_amount >= 0),
    max_discount_amount DECIMAL(10, 2) CHECK (max_discount_amount >= 0),
    usage_limit INTEGER,
    used_count INTEGER DEFAULT 0,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (valid_until > valid_from)
);

-- Create Admin_Activities Table
CREATE TABLE Admin_Activities (
    activity_id SERIAL PRIMARY KEY,
    admin_email VARCHAR(255) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    description TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================

-- Core Table Indexes
CREATE INDEX IX_Products_CategoryId ON Products(category_id);
CREATE INDEX IX_Products_SellerId ON Products(seller_id);
CREATE INDEX IX_ProductVariants_ProductId ON Product_Variants(product_id);
CREATE INDEX IX_CartProducts_VariantId ON Cart_Products(variant_id);
CREATE INDEX IX_CartProducts_CartId ON Cart_Products(cart_id);
CREATE INDEX IX_Orders_Email ON Orders(email);
CREATE INDEX IX_Reviews_ProductId ON Reviews(product_id);
CREATE INDEX IX_Images_ProductId ON Images(product_id);

-- Admin Table Indexes
CREATE INDEX IX_Coupons_Code ON Coupons(coupon_code);
CREATE INDEX IX_Coupons_Active ON Coupons(is_active);
CREATE INDEX IX_Coupons_ValidDates ON Coupons(valid_from, valid_until);
CREATE INDEX IX_AdminActivities_AdminEmail ON Admin_Activities(admin_email);
CREATE INDEX IX_AdminActivities_EntityType ON Admin_Activities(entity_type);
CREATE INDEX IX_AdminActivities_ActionType ON Admin_Activities(action_type);
CREATE INDEX IX_AdminActivities_CreatedAt ON Admin_Activities(created_at);

-- ============================================================
-- TRIGGER FUNCTIONS
-- ============================================================

-- Create trigger function for updating timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- ============================================================
-- TRIGGERS FOR AUTO-UPDATING TIMESTAMPS
-- ============================================================

-- Core Table Triggers
CREATE TRIGGER update_accounts_modtime
    BEFORE UPDATE ON Accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_modtime
    BEFORE UPDATE ON Products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_product_variants_modtime
    BEFORE UPDATE ON Product_Variants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_cart_modtime
    BEFORE UPDATE ON Cart
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_modtime
    BEFORE UPDATE ON Orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shipping_tracking_modtime
    BEFORE UPDATE ON Shipping_Tracking
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Admin Table Triggers
CREATE TRIGGER update_coupons_modtime
    BEFORE UPDATE ON Coupons
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================
-- END OF SCRIPT
-- ============================================================

