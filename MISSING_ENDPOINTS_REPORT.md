# Missing Endpoints Report
## Critical Endpoints Needed to Complete the API

---

## 🔴 **CRITICAL - Must Implement**

### 1. **Create Order from Cart** ⚠️ **HIGHEST PRIORITY**
- **Current Status**: Order creation exists but requires manual cart item mapping
- **Missing**: Endpoint to convert cart to order automatically
- **Needed Endpoint**: `POST /api/orders/from-cart/{cartId}`
- **Functionality**:
  - Get cart by ID
  - Create order from cart items
  - Calculate total price
  - Update stock quantities for each variant
  - Link cart products to order
  - Update order status
- **Why Critical**: Users can't complete purchases - the checkout flow is incomplete

---

### 2. **Get Cart by Email** ⚠️ **HIGH PRIORITY**
- **Current Status**: Repository has `findByAccount_Email` but controller doesn't expose it
- **Missing**: `GET /api/carts/user/{email}`
- **Functionality**: Get user's cart using email (not cart ID)
- **Why Critical**: Frontend needs to fetch cart using logged-in user's email

---

### 3. **Get Orders by Email** ⚠️ **HIGH PRIORITY**
- **Current Status**: Repository has `findByAccount_Email` but controller doesn't expose it
- **Missing**: `GET /api/orders/user/{email}`
- **Functionality**: Get all orders for a specific user
- **Why Critical**: Users need to see their order history

---

### 4. **Get Cart Products by Cart ID** ⚠️ **HIGH PRIORITY**
- **Current Status**: Repository has `findByCartId` but no endpoint
- **Missing**: `GET /api/cart-products/cart/{cartId}`
- **Functionality**: Get all items in a specific cart
- **Why Critical**: Frontend needs to display cart contents

---

### 5. **Image Management Controller** ⚠️ **HIGH PRIORITY**
- **Current Status**: Image entity and repository exist, but NO controller
- **Missing**: Complete `ImageController` with CRUD operations
- **Needed Endpoints**:
  - `GET /api/images` - Get all images
  - `GET /api/images/{id}` - Get image by ID
  - `GET /api/images/product/{productId}` - Get images for a product
  - `POST /api/images` - Create/upload image
  - `PUT /api/images/{id}` - Update image
  - `DELETE /api/images/{id}` - Delete image
- **Why Critical**: Products need images, but there's no way to manage them

---

## 🟡 **IMPORTANT - Should Implement**

### 6. **Product Variants CRUD Operations**
- **Current Status**: Only GET endpoints exist
- **Missing**:
  - `POST /api/variants` - Create variant
  - `PUT /api/variants/{id}` - Update variant (including stock)
  - `DELETE /api/variants/{id}` - Delete variant
- **Functionality**: Full CRUD for product variants
- **Why Important**: Sellers need to manage variants (colors, sizes, stock)

---

### 7. **Products by Category**
- **Current Status**: Repository has `findByCategoryId` but no endpoint
- **Missing**: `GET /api/products/category/{categoryId}`
- **Functionality**: Get all products in a specific category
- **Why Important**: Essential for browsing by category

---

### 8. **Products by Seller**
- **Current Status**: Repository has `findBySellerId` but no endpoint
- **Missing**: `GET /api/products/seller/{sellerId}`
- **Functionality**: Get all products from a specific seller
- **Why Important**: Sellers need to manage their products, customers can browse by seller

---

### 9. **Active Products Only**
- **Current Status**: Repository has `findByIsActive` but no endpoint
- **Missing**: `GET /api/products/active`
- **Query Parameters**: `page`, `size` (pagination)
- **Functionality**: Get only active products (filter out inactive)
- **Why Important**: Frontend should only show active products

---

### 10. **Update Cart Total Price**
- **Current Status**: Cart has `totalPrice` but no automatic calculation
- **Missing**: Logic to calculate cart total from cart products
- **Needed**: 
  - Update total when items added/removed/updated
  - Or endpoint: `PUT /api/carts/{id}/calculate-total`
- **Why Important**: Cart total should be calculated automatically

---

### 11. **Stock Management**
- **Current Status**: Stock quantity exists but no update endpoint
- **Missing**: 
  - `PUT /api/variants/{id}/stock` - Update stock quantity
  - Or include in variant update endpoint
- **Functionality**: Update stock when orders are placed or manually adjusted
- **Why Important**: Inventory management is critical

---

### 12. **Product Search/Filter**
- **Current Status**: Only basic pagination exists
- **Missing**: 
  - `GET /api/products/search?q={query}` - Search products by name/description
  - `GET /api/products/filter?categoryId={id}&sellerId={id}&minPrice={price}&maxPrice={price}`
- **Functionality**: Search and filter products
- **Why Important**: Essential e-commerce feature

---

## 🟢 **NICE TO HAVE - Optional**

### 13. **Reviews Endpoints** (Table exists but unused)
- **Missing**: Complete ReviewController
- **Needed**:
  - `GET /api/reviews/product/{productId}` - Get reviews for product
  - `POST /api/reviews` - Create review
  - `PUT /api/reviews/{id}` - Update review
  - `DELETE /api/reviews/{id}` - Delete review
- **Note**: Reviews table exists in database but no entity/controller

---

### 14. **Shipping Tracking Endpoints** (Table exists but unused)
- **Missing**: Complete ShippingTrackingController
- **Needed**:
  - `GET /api/shipping/{orderId}` - Get tracking for order
  - `POST /api/shipping` - Create tracking entry
  - `PUT /api/shipping/{id}` - Update tracking
- **Note**: Shipping_Tracking table exists but no entity/controller

---

### 15. **Get Images by Variant**
- **Current Status**: Repository only has `findByProductId`
- **Missing**: 
  - Repository method: `findByVariantId(Long variantId)`
  - Endpoint: `GET /api/images/variant/{variantId}`
- **Functionality**: Get images for a specific variant

---

### 16. **Bulk Operations**
- **Missing**:
  - `POST /api/cart-products/bulk` - Add multiple items at once
  - `PUT /api/products/bulk` - Update multiple products
- **Functionality**: Bulk operations for efficiency

---

### 17. **Order Status Update**
- **Current Status**: Can update order but no specific status endpoint
- **Missing**: `PUT /api/orders/{id}/status` - Update only status
- **Functionality**: Simplified status updates

---

### 18. **Cart Clear/Empty**
- **Current Status**: Can delete cart but not clear items
- **Missing**: `DELETE /api/carts/{id}/items` - Clear all items from cart
- **Functionality**: Remove all items but keep cart

---

## 📊 **Summary**

### Endpoints Status:
- ✅ **Fully Implemented**: 42 endpoints
- 🔴 **Critical Missing**: 5 endpoints
- 🟡 **Important Missing**: 7 endpoints  
- 🟢 **Nice to Have**: 6 features

### Priority Breakdown:

**🔴 Must Implement (Critical for MVP):**
1. Create Order from Cart
2. Get Cart by Email
3. Get Orders by Email
4. Get Cart Products by Cart ID
5. Image Management Controller (CRUD)

**🟡 Should Implement (Important for full functionality):**
6. Product Variants CRUD
7. Products by Category
8. Products by Seller
9. Active Products Only
10. Update Cart Total Price
11. Stock Management
12. Product Search/Filter

**🟢 Optional (Enhancement features):**
13-18. Various enhancement endpoints

---

## 🎯 **Recommended Implementation Order**

### Phase 1 - Critical (Complete MVP):
1. Get Cart by Email
2. Get Cart Products by Cart ID
3. Create Order from Cart (with stock updates)
4. Get Orders by Email
5. Image Management Controller

### Phase 2 - Important Features:
6. Products by Category
7. Products by Seller
8. Active Products Only
9. Product Variants CRUD
10. Stock Management
11. Product Search/Filter
12. Auto-calculate Cart Total

### Phase 3 - Enhancements:
13. Reviews System
14. Shipping Tracking
15. Bulk Operations
16. Other nice-to-have features

---

## 📝 **Notes**

- The most critical gap is the **"Create Order from Cart"** endpoint. Currently, orders can be created manually, but there's no automated way to:
  - Convert cart to order
  - Update stock quantities
  - Link cart products to order
  - Handle the complete checkout flow

- **Image Management** is completely missing despite having the entity and repository.

- Many repository methods exist but are not exposed via endpoints, limiting API functionality.

