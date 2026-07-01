-- Migration: Add Many-to-Many relationship between Products and Categories
-- Create product_categories join table
CREATE TABLE IF NOT EXISTS product_categories (
    product_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_product_categories_product 
        FOREIGN KEY (product_id) 
        REFERENCES products(product_id) 
        ON DELETE CASCADE,
    CONSTRAINT fk_product_categories_category 
        FOREIGN KEY (category_id) 
        REFERENCES categories(category_id) 
        ON DELETE CASCADE
);

-- Copy existing product-category relationships from products.category_id to product_categories
INSERT INTO product_categories (product_id, category_id)
SELECT product_id, category_id 
FROM products 
WHERE category_id IS NOT NULL;

-- Drop the foreign key constraint on products.category_id (constraint name from database_complete.sql)
ALTER TABLE products DROP CONSTRAINT IF EXISTS fkog2rp4qthbtt2lfyhfo32lsw9;

-- Drop the category_id column from products table
ALTER TABLE products DROP COLUMN IF EXISTS category_id;

-- Create index on product_categories for better query performance
CREATE INDEX IF NOT EXISTS idx_product_categories_category_id ON product_categories(category_id);
CREATE INDEX IF NOT EXISTS idx_product_categories_product_id ON product_categories(product_id);
