# Database Schema Compatibility Report

## Analysis: postgre_creation.sql vs Java API Entities

---

## ❌ **INCOMPATIBILITIES FOUND**

### 1. **Accounts Table** - Missing Column
- **SQL Schema**: Does NOT have `updated_at` column
- **Java Entity**: `Account.java` has `updatedAt` field (line 43-44)
- **Impact**: ⚠️ **CRITICAL** - Entity tries to map a field that doesn't exist in database
- **Fix Required**: Add `updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP` to Accounts table

---

### 2. **Sellers Table** - Missing Column
- **SQL Schema**: Does NOT have `is_active` column
- **Java Entity**: `Seller.java` has `isActive` field (line 35-36)
- **Impact**: ⚠️ **CRITICAL** - Entity tries to map a field that doesn't exist in database
- **Fix Required**: Add `is_active BOOLEAN DEFAULT TRUE` to Sellers table

---

### 3. **Cart Table** - Missing Columns
- **SQL Schema**: Only has `cart_id`, `email`, `created_at`, `updated_at`
- **Java Entity**: `Cart.java` has additional fields:
  - `totalPrice` (line 31-32) - **MISSING in SQL**
  - `isActive` (line 34-35) - **MISSING in SQL**
- **Impact**: ⚠️ **CRITICAL** - Entity tries to map fields that don't exist in database
- **Fix Required**: 
  ```sql
  ALTER TABLE Cart ADD COLUMN total_price DECIMAL(10, 2) DEFAULT 0.00;
  ALTER TABLE Cart ADD COLUMN is_active BOOLEAN DEFAULT TRUE;
  ```

---

## ✅ **COMPATIBLE TABLES**

### 4. **Categories Table** ✅
- All columns match between SQL and Java entity
- Table name: `categories` (case-insensitive match)

### 5. **Products Table** ✅
- All columns match between SQL and Java entity
- Table name: `products` (case-insensitive match)

### 6. **Product_Variants Table** ✅
- All columns match between SQL and Java entity
- Table name: `product_variants` (case-insensitive match)

### 7. **Images Table** ✅
- All columns match between SQL and Java entity
- Table name: `images` (case-insensitive match)

### 8. **Cart_Products Table** ✅
- All columns match between SQL and Java entity
- Table name: `cart_products` (case-insensitive match)

### 9. **Orders Table** ✅
- All columns match between SQL and Java entity
- Table name: `orders` (case-insensitive match)

---

## ⚠️ **UNUSED TABLES IN DATABASE**

### 10. **Reviews Table**
- **SQL Schema**: Table exists (lines 126-136)
- **Java API**: ❌ **NO ENTITY** - No `Review.java` model exists
- **Impact**: ⚠️ **WARNING** - Table exists but not used by API
- **Recommendation**: Either create a Review entity and controller, or remove the table if not needed

### 11. **Shipping_Tracking Table**
- **SQL Schema**: Table exists (lines 138-150)
- **Java API**: ❌ **NO ENTITY** - No `ShippingTracking.java` model exists
- **Impact**: ⚠️ **WARNING** - Table exists but not used by API
- **Recommendation**: Either create a ShippingTracking entity and controller, or remove the table if not needed

---

## 📋 **DETAILED COLUMN COMPARISON**

### Accounts Table
| Column Name | SQL | Java Entity | Status |
|------------|-----|-------------|--------|
| email | ✅ VARCHAR(255) PRIMARY KEY | ✅ String email | ✅ Match |
| first_name | ✅ VARCHAR(255) | ✅ String firstName | ✅ Match |
| last_name | ✅ VARCHAR(255) | ✅ String lastName | ✅ Match |
| password | ✅ VARCHAR(255) NOT NULL | ✅ String password | ✅ Match |
| phone_num | ✅ VARCHAR(15) | ✅ String phoneNum | ✅ Match |
| seller_account | ✅ BOOLEAN DEFAULT FALSE | ✅ Boolean sellerAccount | ✅ Match |
| created_at | ✅ TIMESTAMP | ✅ LocalDateTime createdAt | ✅ Match |
| last_login | ✅ TIMESTAMP | ✅ LocalDateTime lastLogin | ✅ Match |
| is_active | ✅ BOOLEAN DEFAULT TRUE | ✅ Boolean isActive | ✅ Match |
| updated_at | ❌ **MISSING** | ✅ LocalDateTime updatedAt | ❌ **ERROR** |

### Sellers Table
| Column Name | SQL | Java Entity | Status |
|------------|-----|-------------|--------|
| seller_id | ✅ SERIAL PRIMARY KEY | ✅ Long sellerId | ✅ Match |
| seller_name | ✅ VARCHAR(255) NOT NULL | ✅ String sellerName | ✅ Match |
| email | ✅ VARCHAR(255) NOT NULL | ✅ String email | ✅ Match |
| created_at | ✅ TIMESTAMP | ✅ LocalDateTime createdAt | ✅ Match |
| rating | ✅ DECIMAL(3,2) | ✅ BigDecimal rating | ✅ Match |
| is_active | ❌ **MISSING** | ✅ Boolean isActive | ❌ **ERROR** |

### Cart Table
| Column Name | SQL | Java Entity | Status |
|------------|-----|-------------|--------|
| cart_id | ✅ SERIAL PRIMARY KEY | ✅ Long cartId | ✅ Match |
| email | ✅ VARCHAR(255) NOT NULL | ✅ String email | ✅ Match |
| created_at | ✅ TIMESTAMP | ✅ LocalDateTime createdAt | ✅ Match |
| updated_at | ✅ TIMESTAMP | ✅ LocalDateTime updatedAt | ✅ Match |
| total_price | ❌ **MISSING** | ✅ BigDecimal totalPrice | ❌ **ERROR** |
| is_active | ❌ **MISSING** | ✅ Boolean isActive | ❌ **ERROR** |

---

## 🔧 **REQUIRED SQL FIXES**

Add these SQL statements to fix the incompatibilities:

```sql
-- Fix Accounts table
ALTER TABLE Accounts ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Fix Sellers table
ALTER TABLE Sellers ADD COLUMN is_active BOOLEAN DEFAULT TRUE;

-- Fix Cart table
ALTER TABLE Cart ADD COLUMN total_price DECIMAL(10, 2) DEFAULT 0.00;
ALTER TABLE Cart ADD COLUMN is_active BOOLEAN DEFAULT TRUE;

-- Optional: Add trigger for Accounts updated_at (if needed)
CREATE TRIGGER update_accounts_modtime
    BEFORE UPDATE ON Accounts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

## ✅ **UPDATED SQL SCRIPT RECOMMENDATION**

Update `postgre_creation.sql` with the following changes:

1. **Line 19** (Accounts table) - Add:
   ```sql
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   ```

2. **Line 29** (Sellers table) - Add:
   ```sql
   is_active BOOLEAN DEFAULT TRUE,
   ```

3. **Line 91** (Cart table) - Change from:
   ```sql
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   FOREIGN KEY (email) REFERENCES Accounts(email) ON DELETE CASCADE
   ```
   To:
   ```sql
   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   total_price DECIMAL(10, 2) DEFAULT 0.00,
   is_active BOOLEAN DEFAULT TRUE,
   FOREIGN KEY (email) REFERENCES Accounts(email) ON DELETE CASCADE
   ```

4. **Line 190** (after Orders trigger) - Add:
   ```sql
   CREATE TRIGGER update_accounts_modtime
       BEFORE UPDATE ON Accounts
       FOR EACH ROW
       EXECUTE FUNCTION update_updated_at_column();
   ```

---

## 🎯 **SUMMARY**

### Critical Issues (Will Cause Runtime Errors)
- ❌ Accounts table missing `updated_at` column
- ❌ Sellers table missing `is_active` column  
- ❌ Cart table missing `total_price` column
- ❌ Cart table missing `is_active` column

### Warnings (Not Used But Won't Break)
- ⚠️ Reviews table exists but no API endpoint uses it
- ⚠️ Shipping_Tracking table exists but no API endpoint uses it

### Compatibility Status
- **6 out of 9** tables are fully compatible
- **3 tables** need fixes before API can work correctly
- **2 tables** exist in database but are not used by API

---

## 📝 **NEXT STEPS**

1. ✅ **URGENT**: Apply the SQL fixes above to make the schema compatible
2. ⚠️ **RECOMMENDED**: Decide whether to implement Reviews and Shipping_Tracking features or remove the tables
3. ✅ **TEST**: Run the application after applying fixes to verify compatibility

