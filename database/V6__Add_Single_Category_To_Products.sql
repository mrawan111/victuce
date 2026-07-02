-- Add category_id column to products table for single category + inheritance
ALTER TABLE products ADD COLUMN IF NOT EXISTS category_id bigint;

-- Add foreign key constraint
ALTER TABLE products 
ADD CONSTRAINT fk_products_category 
FOREIGN KEY (category_id) REFERENCES categories(category_id) 
ON DELETE SET NULL 
ON UPDATE CASCADE;

-- Add index for performance
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
