-- Coupons Table
CREATE TABLE IF NOT EXISTS coupons (
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

-- Admin Activities Table
CREATE TABLE IF NOT EXISTS admin_activities (
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

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS IX_Coupons_Code ON coupons(coupon_code);
CREATE INDEX IF NOT EXISTS IX_Coupons_Active ON coupons(is_active);
CREATE INDEX IF NOT EXISTS IX_Coupons_ValidDates ON coupons(valid_from, valid_until);
CREATE INDEX IF NOT EXISTS IX_AdminActivities_AdminEmail ON admin_activities(admin_email);
CREATE INDEX IF NOT EXISTS IX_AdminActivities_EntityType ON admin_activities(entity_type);
CREATE INDEX IF NOT EXISTS IX_AdminActivities_ActionType ON admin_activities(action_type);
CREATE INDEX IF NOT EXISTS IX_AdminActivities_CreatedAt ON admin_activities(created_at);

-- Create trigger for updating coupons updated_at
CREATE OR REPLACE FUNCTION update_coupons_modtime()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_coupons_modtime
    BEFORE UPDATE ON coupons
    FOR EACH ROW
    EXECUTE FUNCTION update_coupons_modtime();

