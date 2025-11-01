# New API Features Documentation
## Cart Sync, Admin Coupons, and Admin Activity Logging

**Date**: Latest Update  
**Version**: 2.0  
**New Endpoints**: 16 additional endpoints

---

## 📋 Table of Contents
1. [Cart Sync Functionality](#cart-sync-functionality)
2. [Admin Coupons Management](#admin-coupons-management)
3. [Admin Activity Logging](#admin-activity-logging)
4. [Summary of Changes](#summary-of-changes)

---

## 🛒 Cart Sync Functionality

### Overview
Enhanced cart management with automatic synchronization and total calculation capabilities.

---

### 1. Sync Cart
**Endpoint**: `POST /api/carts/sync`  
**Description**: Synchronizes a user's cart with the backend. Creates a cart if it doesn't exist, calculates total from cart items, and returns cart summary.  
**Authentication**: Not required (can be added)

**Request Body**:
```json
{
  "email": "user@example.com"
}
```

**Response** (200 OK):
```json
{
  "cart_id": 123,
  "email": "user@example.com",
  "total_price": 299.97,
  "item_count": 3,
  "synced": true
}
```

**Error Responses**:
- `400 Bad Request`: "Email is required"
- `500 Internal Server Error`: Error message

**Use Case**: 
- Frontend can call this when user loads cart page
- Ensures cart exists and totals are accurate
- Returns complete cart state for display

---

### 2. Calculate Cart Total
**Endpoint**: `PUT /api/carts/{id}/calculate-total`  
**Description**: Recalculates and updates the cart total price based on all cart products. Useful after adding/removing items.  
**Authentication**: Not required (can be added)

**Path Parameters**:
- `id`: Long (required) - Cart ID

**Response** (200 OK):
```json
{
  "cart_id": 123,
  "total_price": 299.97,
  "item_count": 3
}
```

**Error Responses**:
- `404 Not Found`: "Cart not found"
- `500 Internal Server Error`: Error message

**Use Case**:
- Call after adding/removing items from cart
- Call after updating quantities
- Ensures cart total is always accurate

---

## 🎟️ Admin Coupons Management

### Overview
Complete coupon management system with validation, discount calculation, and usage tracking.

---

### 1. Get All Coupons
**Endpoint**: `GET /api/admin/coupons`  
**Description**: Retrieves all coupons (active and inactive).  
**Authentication**: Admin required (to be implemented)

**Response** (200 OK):
```json
[
  {
    "couponId": 1,
    "couponCode": "SAVE20",
    "description": "20% off on all items",
    "discountType": "PERCENTAGE",
    "discountValue": 20.00,
    "minPurchaseAmount": 50.00,
    "maxDiscountAmount": 100.00,
    "usageLimit": 100,
    "usedCount": 45,
    "validFrom": "2024-01-01T00:00:00",
    "validUntil": "2024-12-31T23:59:59",
    "isActive": true,
    "createdAt": "2024-01-01T00:00:00",
    "updatedAt": "2024-01-01T00:00:00"
  }
]
```

---

### 2. Get Active Coupons
**Endpoint**: `GET /api/admin/coupons/active`  
**Description**: Retrieves only active coupons.  
**Authentication**: Admin required (to be implemented)

**Response** (200 OK): Array of active Coupon objects

---

### 3. Get Coupon by ID
**Endpoint**: `GET /api/admin/coupons/{id}`  
**Description**: Retrieves a specific coupon by ID.

**Path Parameters**:
- `id`: Long (required)

**Response** (200 OK): Coupon object

**Error Response** (404):
```json
{
  "message": "Coupon not found"
}
```

---

### 4. Get Coupon by Code
**Endpoint**: `GET /api/admin/coupons/code/{code}`  
**Description**: Retrieves a coupon by its code (case-insensitive).

**Path Parameters**:
- `code`: String (required) - Coupon code

**Response** (200 OK): Coupon object

**Error Response** (404):
```json
{
  "message": "Coupon not found"
}
```

---

### 5. Create Coupon
**Endpoint**: `POST /api/admin/coupons`  
**Description**: Creates a new coupon. Automatically converts coupon code to uppercase.

**Request Body**:
```json
{
  "couponCode": "SAVE20" (required, unique),
  "description": "20% off on all items" (optional),
  "discountType": "PERCENTAGE" (required, must be "PERCENTAGE" or "FIXED"),
  "discountValue": 20.00 (required, must be >= 0),
  "minPurchaseAmount": 50.00 (optional, must be >= 0),
  "maxDiscountAmount": 100.00 (optional, for percentage discounts),
  "usageLimit": 100 (optional),
  "validFrom": "2024-01-01T00:00:00" (required),
  "validUntil": "2024-12-31T23:59:59" (required),
  "isActive": true (optional, default: true)
}
```

**Response** (201 Created): Created Coupon object

**Error Responses**:
- `400 Bad Request`: "Coupon code is required"
- `400 Bad Request`: "Coupon code already exists"
- `400 Bad Request`: "Discount type must be 'PERCENTAGE' or 'FIXED'"
- `400 Bad Request`: "Valid until date must be after valid from date"

**Validation Rules**:
- Coupon code must be unique
- Discount type must be PERCENTAGE or FIXED
- Valid until must be after valid from
- All decimal values must be >= 0

---

### 6. Update Coupon
**Endpoint**: `PUT /api/admin/coupons/{id}`  
**Description**: Updates an existing coupon. Only provided fields will be updated.

**Path Parameters**:
- `id`: Long (required)

**Request Body** (all fields optional):
```json
{
  "couponCode": "NEWCODE" (optional),
  "description": "Updated description" (optional),
  "discountType": "FIXED" (optional),
  "discountValue": 10.00 (optional),
  "minPurchaseAmount": 25.00 (optional),
  "maxDiscountAmount": 50.00 (optional),
  "usageLimit": 50 (optional),
  "validFrom": "2024-02-01T00:00:00" (optional),
  "validUntil": "2024-12-31T23:59:59" (optional),
  "isActive": false (optional)
}
```

**Response** (200 OK): Updated Coupon object

**Error Responses**:
- `404 Not Found`: "Coupon not found"
- `400 Bad Request`: "Coupon code already exists"
- `400 Bad Request`: "Discount type must be 'PERCENTAGE' or 'FIXED'"
- `400 Bad Request`: "Valid until date must be after valid from date"

---

### 7. Delete Coupon
**Endpoint**: `DELETE /api/admin/coupons/{id}`  
**Description**: Deletes a coupon.

**Path Parameters**:
- `id`: Long (required)

**Response** (200 OK):
```json
{
  "message": "Coupon deleted successfully"
}
```

**Error Response** (404):
```json
{
  "message": "Coupon not found"
}
```

---

### 8. Validate Coupon ⭐ **IMPORTANT**
**Endpoint**: `POST /api/admin/coupons/validate/{code}`  
**Description**: Validates a coupon code and calculates discount. This endpoint can be used by frontend to:
- Check if coupon is valid
- Calculate discount amount
- Get final price after discount

**Path Parameters**:
- `code`: String (required) - Coupon code to validate

**Request Body**:
```json
{
  "cart_total": 150.00 (required)
}
```

**Response** (200 OK - Valid Coupon):
```json
{
  "valid": true,
  "coupon_code": "SAVE20",
  "discount": 30.00,
  "discount_type": "PERCENTAGE",
  "original_amount": 150.00,
  "final_amount": 120.00
}
```

**Response** (200 OK - Invalid Coupon):
```json
{
  "valid": false,
  "error": "Coupon has expired"
}
```

**Validation Checks**:
1. ✅ Coupon exists
2. ✅ Coupon is active
3. ✅ Current date is within valid range
4. ✅ Usage limit not exceeded
5. ✅ Minimum purchase amount met
6. ✅ Discount calculation (respects max discount for percentage)

**Error Responses**:
- `404 Not Found`: Coupon not found
- `400 Bad Request`: Error message

**Discount Calculation Examples**:

**Percentage Discount (with max)**:
- Cart Total: $200
- Discount: 20%
- Calculated: $40
- Max Discount: $30
- **Final Discount**: $30
- **Final Amount**: $170

**Percentage Discount (no max)**:
- Cart Total: $100
- Discount: 15%
- **Final Discount**: $15
- **Final Amount**: $85

**Fixed Discount**:
- Cart Total: $50
- Discount: $75 (fixed)
- **Final Discount**: $50 (cannot exceed cart total)
- **Final Amount**: $0

---

## 📊 Admin Activity Logging

### Overview
Complete activity logging system to track all admin actions for audit and security purposes.

---

### 1. Get All Activities
**Endpoint**: `GET /api/admin/activities`  
**Description**: Retrieves all admin activities with pagination.  
**Authentication**: Admin required (to be implemented)

**Query Parameters**:
- `page`: integer (optional, default: 0)
- `size`: integer (optional, default: 20)

**Response** (200 OK):
```json
{
  "content": [
    {
      "activityId": 1,
      "adminEmail": "admin@example.com",
      "actionType": "CREATE",
      "entityType": "PRODUCT",
      "entityId": 123,
      "description": "Created new product: iPhone 15",
      "ipAddress": "192.168.1.1",
      "userAgent": "Mozilla/5.0...",
      "createdAt": "2024-01-15T10:30:00"
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

---

### 2. Get Activity by ID
**Endpoint**: `GET /api/admin/activities/{id}`  
**Description**: Retrieves a specific activity by ID.

**Path Parameters**:
- `id`: Long (required)

**Response** (200 OK): AdminActivity object

**Error Response** (404):
```json
{
  "message": "Activity not found"
}
```

---

### 3. Get Activities by Admin Email
**Endpoint**: `GET /api/admin/activities/admin/{email}`  
**Description**: Retrieves all activities for a specific admin with pagination.

**Path Parameters**:
- `email`: String (required)

**Query Parameters**:
- `page`: integer (optional, default: 0)
- `size`: integer (optional, default: 20)

**Response** (200 OK): Paginated AdminActivity objects

---

### 4. Get Activities by Entity Type
**Endpoint**: `GET /api/admin/activities/entity/{entityType}`  
**Description**: Retrieves all activities for a specific entity type (e.g., PRODUCT, ORDER, COUPON).

**Path Parameters**:
- `entityType`: String (required) - Examples: PRODUCT, ORDER, COUPON, USER

**Response** (200 OK): Array of AdminActivity objects

**Example**:
```
GET /api/admin/activities/entity/PRODUCT
```
Returns all activities related to products.

---

### 5. Get Activities by Action Type
**Endpoint**: `GET /api/admin/activities/action/{actionType}`  
**Description**: Retrieves all activities for a specific action type (e.g., CREATE, UPDATE, DELETE).

**Path Parameters**:
- `actionType`: String (required) - Examples: CREATE, UPDATE, DELETE, VIEW

**Response** (200 OK): Array of AdminActivity objects

**Example**:
```
GET /api/admin/activities/action/DELETE
```
Returns all delete activities.

---

### 6. Log Activity (Full Object)
**Endpoint**: `POST /api/admin/activities`  
**Description**: Logs an admin activity using full AdminActivity object. Automatically captures IP address and User-Agent from request.

**Request Body**:
```json
{
  "adminEmail": "admin@example.com" (required),
  "actionType": "CREATE" (required),
  "entityType": "PRODUCT" (required),
  "entityId": 123 (optional),
  "description": "Created new product: iPhone 15" (optional)
}
```

**Response** (201 Created): Created AdminActivity object with:
- Auto-filled `ipAddress`
- Auto-filled `userAgent`
- Auto-filled `createdAt`

**Error Responses**:
- `400 Bad Request`: "Admin email is required"
- `400 Bad Request`: "Action type is required"
- `400 Bad Request`: "Entity type is required"

**Note**: IP address and User-Agent are automatically extracted from the HTTP request.

---

### 7. Quick Log Activity ⭐ **RECOMMENDED**
**Endpoint**: `POST /api/admin/activities/quick-log`  
**Description**: Simplified activity logging endpoint. Easier to use from frontend.

**Request Body**:
```json
{
  "admin_email": "admin@example.com" (required),
  "action_type": "UPDATE" (required),
  "entity_type": "ORDER" (required),
  "entity_id": 456 (optional),
  "description": "Updated order status to shipped" (optional)
}
```

**Response** (201 Created): Created AdminActivity object

**Error Responses**:
- `400 Bad Request`: "Admin email is required"
- `400 Bad Request`: "Action type is required"
- `400 Bad Request`: "Entity type is required"

**Use Case Examples**:

**Product Created**:
```json
{
  "admin_email": "admin@example.com",
  "action_type": "CREATE",
  "entity_type": "PRODUCT",
  "entity_id": 789,
  "description": "Created product: Samsung Galaxy S24"
}
```

**Order Updated**:
```json
{
  "admin_email": "admin@example.com",
  "action_type": "UPDATE",
  "entity_type": "ORDER",
  "entity_id": 123,
  "description": "Changed order status from pending to shipped"
}
```

**User Deleted**:
```json
{
  "admin_email": "admin@example.com",
  "action_type": "DELETE",
  "entity_type": "USER",
  "entity_id": 456,
  "description": "Deleted user account: user@example.com"
}
```

---

### 8. Delete Activity
**Endpoint**: `DELETE /api/admin/activities/{id}`  
**Description**: Deletes an activity log entry.

**Path Parameters**:
- `id`: Long (required)

**Response** (200 OK):
```json
{
  "message": "Activity deleted successfully"
}
```

**Error Response** (404):
```json
{
  "message": "Activity not found"
}
```

---

## 📋 Activity Types Reference

### Action Types
- `CREATE` - Creating new entity
- `UPDATE` - Updating existing entity
- `DELETE` - Deleting entity
- `VIEW` - Viewing/accessing entity
- `LOGIN` - Admin login
- `LOGOUT` - Admin logout
- `APPROVE` - Approving something
- `REJECT` - Rejecting something

### Entity Types
- `PRODUCT` - Product related actions
- `ORDER` - Order related actions
- `COUPON` - Coupon related actions
- `USER` - User/Account related actions
- `CATEGORY` - Category related actions
- `SELLER` - Seller related actions
- `CART` - Cart related actions
- `SYSTEM` - System configuration actions

---

## 📊 Summary of Changes

### New Endpoints Added: **16 Total**

#### Cart Sync (2 endpoints)
1. `POST /api/carts/sync` - Sync cart with backend
2. `PUT /api/carts/{id}/calculate-total` - Calculate cart total

#### Admin Coupons (8 endpoints)
1. `GET /api/admin/coupons` - Get all coupons
2. `GET /api/admin/coupons/active` - Get active coupons
3. `GET /api/admin/coupons/{id}` - Get coupon by ID
4. `GET /api/admin/coupons/code/{code}` - Get coupon by code
5. `POST /api/admin/coupons` - Create coupon
6. `PUT /api/admin/coupons/{id}` - Update coupon
7. `DELETE /api/admin/coupons/{id}` - Delete coupon
8. `POST /api/admin/coupons/validate/{code}` - Validate coupon

#### Admin Activity (8 endpoints)
1. `GET /api/admin/activities` - Get all activities (paginated)
2. `GET /api/admin/activities/{id}` - Get activity by ID
3. `GET /api/admin/activities/admin/{email}` - Get by admin email
4. `GET /api/admin/activities/entity/{entityType}` - Get by entity type
5. `GET /api/admin/activities/action/{actionType}` - Get by action type
6. `POST /api/admin/activities` - Log activity (full object)
7. `POST /api/admin/activities/quick-log` - Quick log activity
8. `DELETE /api/admin/activities/{id}` - Delete activity

### New Database Tables
1. **Coupons** - Coupon management
2. **Admin_Activities** - Activity logging

### Models Created
1. `Coupon.java` - Coupon entity
2. `AdminActivity.java` - Activity log entity

### Repositories Created
1. `CouponRepository.java`
2. `AdminActivityRepository.java`

### Controllers Created
1. `AdminCouponController.java`
2. `AdminActivityController.java`

### Enhanced Controllers
1. `CartController.java` - Added sync and calculate-total endpoints

---

## 🔐 Security Notes

**Current Status**: 
- All endpoints are currently **publicly accessible**
- Authentication/authorization should be added for admin endpoints

**Recommended Security Enhancements**:
1. Add JWT authentication for admin endpoints
2. Add role-based access control (RBAC)
3. Validate admin permissions before allowing operations
4. Rate limiting for activity logging

---

## 🚀 Integration Examples

### Frontend Cart Sync
```javascript
// Sync cart when page loads
async function syncCart(email) {
  const response = await fetch('/api/carts/sync', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email })
  });
  const cart = await response.json();
  return cart;
}
```

### Validate Coupon in Checkout
```javascript
async function validateCoupon(code, cartTotal) {
  const response = await fetch(`/api/admin/coupons/validate/${code}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ cart_total: cartTotal })
  });
  const result = await response.json();
  if (result.valid) {
    // Apply discount
    console.log(`Discount: $${result.discount}`);
    console.log(`Final Amount: $${result.final_amount}`);
  } else {
    console.error(result.error);
  }
}
```

### Log Admin Activity
```javascript
async function logActivity(adminEmail, actionType, entityType, entityId, description) {
  await fetch('/api/admin/activities/quick-log', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      admin_email: adminEmail,
      action_type: actionType,
      entity_type: entityType,
      entity_id: entityId,
      description: description
    })
  });
}
```

---

## 📝 Migration Notes

### Database Setup
Run the complete database script:
```bash
psql -U postgres -f database_complete.sql
```

Or use Hibernate auto-update (if `ddl-auto=update` is set):
- Tables will be created automatically on application startup

### API Versioning
- All new endpoints are under `/api/admin/` prefix
- Cart endpoints enhanced under existing `/api/carts/` path
- No breaking changes to existing endpoints

---

## ✅ Testing Checklist

### Cart Sync
- [ ] Test sync with existing cart
- [ ] Test sync with non-existent cart (creates new)
- [ ] Test calculate total with multiple items
- [ ] Test calculate total with empty cart

### Coupons
- [ ] Test create coupon (percentage)
- [ ] Test create coupon (fixed)
- [ ] Test validate valid coupon
- [ ] Test validate expired coupon
- [ ] Test validate coupon with usage limit reached
- [ ] Test validate coupon with min purchase requirement
- [ ] Test update coupon
- [ ] Test delete coupon

### Activity Logging
- [ ] Test log activity with full object
- [ ] Test quick log activity
- [ ] Test get activities by admin
- [ ] Test get activities by entity type
- [ ] Test get activities by action type
- [ ] Test pagination

---

**Last Updated**: Latest  
**API Version**: 2.0  
**Total Endpoints**: 64 (48 existing + 16 new)

