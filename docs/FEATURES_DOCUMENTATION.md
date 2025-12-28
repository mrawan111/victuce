# VictusStore - Complete Features Documentation

**Version**: 2.0  
**Last Updated**: Latest  
**Spring Boot Version**: 3.1.5  
**Java Version**: 17

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [Core Features](#core-features)
   - [Authentication & Authorization](#1-authentication--authorization)
   - [Account Management](#2-account-management)
   - [Seller Management](#3-seller-management)
   - [Category Management](#4-category-management)
   - [Product Management](#5-product-management)
   - [Product Variant Management](#6-product-variant-management)
   - [Shopping Cart System](#7-shopping-cart-system)
   - [Order Management](#8-order-management)
   - [Image Management](#9-image-management)
   - [Admin Coupon System](#10-admin-coupon-system)
   - [Admin Activity Logging](#11-admin-activity-logging)
4. [API Endpoints Summary](#api-endpoints-summary)
5. [Database Schema](#database-schema)
6. [Security Features](#security-features)
7. [Integration Guide](#integration-guide)
8. [Deployment](#deployment)

---

## Project Overview

VictusStore is a comprehensive e-commerce backend API built with Spring Boot. It provides a complete solution for managing an online store, including user authentication, product catalog, shopping cart, order processing, and administrative features.

### Key Highlights

- ✅ **Complete E-Commerce Flow**: From product browsing to order completion
- ✅ **JWT Authentication**: Secure token-based authentication
- ✅ **Shopping Cart**: Full cart management with synchronization
- ✅ **Order Processing**: Complete checkout flow with stock management
- ✅ **Admin Features**: Coupon management and activity logging
- ✅ **Image Management**: Cloudinary integration for product images
- ✅ **RESTful API**: 64+ endpoints following REST conventions
- ✅ **Swagger Documentation**: Interactive API documentation

---

## Technology Stack

### Backend Framework
- **Spring Boot 3.1.5**: Main application framework
- **Spring Data JPA**: Database abstraction layer
- **Hibernate**: ORM for database operations
- **Spring Security**: Security framework

### Database
- **PostgreSQL**: Primary database

### Authentication
- **JWT (JSON Web Tokens)**: Token-based authentication
- **BCrypt**: Password hashing

### Documentation
- **Swagger/OpenAPI 3**: API documentation

### Cloud Services
- **Cloudinary**: Image storage and management

### Build Tools
- **Maven**: Dependency management and build tool
- **Lombok**: Code generation (getters, setters, builders)
- **MapStruct**: Object mapping

---

## Core Features

### 1. Authentication & Authorization

#### Overview
Secure user authentication system with JWT tokens and password encryption.

#### Features Implemented

**1.1 User Registration**
- Email-based registration
- Password validation (minimum 8 characters)
- Automatic password hashing with BCrypt
- Optional seller account creation
- Phone number validation and formatting
- Duplicate email prevention
- Automatic JWT token generation

**1.2 User Login**
- Email and password authentication
- Account status validation (active/inactive)
- Last login timestamp tracking
- JWT token generation with user details
- Seller account flag in token

**1.3 Account Verification**
- Account existence check
- Password verification
- Seller account status check

#### Technical Details
- **Password Security**: BCrypt hashing with salt
- **Token Expiry**: 24 hours
- **Token Claims**: Email and seller account status
- **CORS**: Enabled for all origins

#### Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `GET /api/auth/check_account/{email}/{password}` - Verify account

---

### 2. Account Management

#### Overview
Complete CRUD operations for user accounts with profile management.

#### Features Implemented

**2.1 Account Operations**
- Create new accounts
- Retrieve account by email
- Update account information
- Delete accounts
- List all accounts
- Account activation/deactivation

**2.2 Account Fields**
- Email (unique identifier)
- Password (hashed)
- First name
- Last name
- Phone number
- Seller account flag
- Active status
- Created timestamp
- Last login timestamp

#### Endpoints
- `GET /api/accounts` - Get all accounts
- `GET /api/accounts/{email}` - Get account by email
- `POST /api/accounts` - Create account
- `PUT /api/accounts/{email}` - Update account
- `DELETE /api/accounts/{email}` - Delete account

---

### 3. Seller Management

#### Overview
Management system for seller accounts and profiles.

#### Features Implemented

**3.1 Seller Operations**
- Create seller profiles
- Retrieve seller information
- Update seller details
- Delete seller accounts
- Seller rating system
- Seller status management

**3.2 Seller Fields**
- Seller ID (auto-generated)
- Seller name
- Email (linked to account)
- Rating (decimal)
- Active status
- Created timestamp

#### Integration
- Automatically created during registration if `seller_account` is true
- Linked to user account via email

#### Endpoints
- `GET /api/sellers` - Get all sellers
- `GET /api/sellers/{id}` - Get seller by ID
- `POST /api/sellers` - Create seller
- `PUT /api/sellers/{id}` - Update seller
- `DELETE /api/sellers/{id}` - Delete seller

---

### 4. Category Management

#### Overview
Hierarchical category system for organizing products.

#### Features Implemented

**4.1 Category Operations**
- Create categories
- Retrieve categories
- Update category information
- Delete categories
- Category hierarchy support (parent categories)
- Category images
- Category activation/deactivation

**4.2 Category Fields**
- Category ID
- Category name (unique)
- Category image URL
- Parent category ID (optional, for hierarchy)
- Active status
- Created timestamp

#### Use Cases
- Organize products into categories
- Support nested category structures
- Display category images in frontend
- Enable/disable categories

#### Endpoints
- `GET /api/categories` - Get all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

---

### 5. Product Management

#### Overview
Complete product catalog management with pagination and relationships.

#### Features Implemented

**5.1 Product Operations**
- Create products
- Retrieve products (with pagination)
- Get product by ID (with full details)
- Update product information
- Delete products
- Product rating system
- Product status management

**5.2 Product Fields**
- Product ID
- Product name
- Description
- Base price
- Category ID (foreign key)
- Seller ID (foreign key)
- Product rating
- Active status
- Created/updated timestamps

**5.3 Advanced Features**
- Pagination support (page, size parameters)
- Relationship loading (category, seller, variants, images)
- Price management
- Stock tracking through variants

#### Endpoints
- `GET /api/products` - Get all products (paginated)
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

---

### 6. Product Variant Management

#### Overview
Product variant system for managing different sizes, colors, and pricing.

#### Features Implemented

**6.1 Variant Operations**
- Create variants for products
- Retrieve variants
- Get variants by product ID
- Update variant information
- Check variant availability
- Stock quantity management
- Variant-specific pricing

**6.2 Variant Fields**
- Variant ID
- Product ID (foreign key)
- Color
- Size
- Stock quantity
- Price (variant-specific, can differ from base price)
- SKU (optional, unique)
- Active status
- Created/updated timestamps

**6.3 Stock Management**
- Real-time stock checking
- Stock quantity updates
- Availability validation
- Automatic stock reduction on order

#### Use Cases
- Different sizes (S, M, L, XL)
- Different colors
- Variant-specific pricing
- Stock tracking per variant
- SKU management

#### Endpoints
- `GET /api/variants` - Get all variants
- `GET /api/variants/{id}` - Get variant by ID
- `GET /api/variants/product/{productId}` - Get variants by product
- `GET /api/variants/{id}/check-availability` - Check stock availability
- `POST /api/variants` - Create variant
- `PUT /api/variants/{id}` - Update variant

---

### 7. Shopping Cart System

#### Overview
Complete shopping cart management with synchronization and automatic total calculation.

#### Features Implemented

**7.1 Cart Operations**
- Create shopping carts
- Retrieve cart by ID
- Get cart by user email
- Update cart information
- Delete carts
- **Cart synchronization** (NEW)
- **Automatic total calculation** (NEW)

**7.2 Cart Synchronization**
- Creates cart if doesn't exist
- Calculates total from cart items
- Returns cart summary with item count
- Validates account existence

**7.3 Cart Total Calculation**
- Recalculates total based on cart products
- Uses `priceAtTime` for accurate pricing
- Updates cart total automatically
- Returns item count

**7.4 Cart Fields**
- Cart ID
- User email
- Total price (auto-calculated)
- Active status
- Created/updated timestamps

#### Integration Points
- Linked to user account via email
- Contains cart products (items)
- Can be cleared after order creation
- Supports multiple items

#### Endpoints
- `GET /api/carts` - Get all carts
- `GET /api/carts/{id}` - Get cart by ID
- `GET /api/carts/user/{email}` - Get cart by email ⭐
- `POST /api/carts` - Create cart
- `PUT /api/carts/{id}` - Update cart
- `DELETE /api/carts/{id}` - Delete cart
- `POST /api/carts/sync` - Sync cart ⭐ NEW
- `PUT /api/carts/{id}/calculate-total` - Calculate total ⭐ NEW

---

### 7.5 Cart Product Management

#### Overview
Management of individual items within shopping carts.

#### Features Implemented

**7.5.1 Cart Product Operations**
- Add products to cart
- Update product quantities
- Remove products from cart
- Retrieve cart items
- Get items by cart ID

**7.5.2 Add to Cart Logic**
- Validates variant existence
- Checks stock availability
- If variant already in cart, increases quantity
- Stores `priceAtTime` for historical pricing
- Validates cart existence
- Updates cart total

**7.5.3 Quantity Management**
- Update item quantities
- Stock validation before update
- Automatic cart total recalculation
- Prevents over-ordering

**7.5.4 Cart Product Fields**
- Cart product ID
- Variant ID (foreign key)
- Cart ID (foreign key)
- Order ID (set when order is created)
- Quantity
- Price at time (preserves historical pricing)
- Created timestamp

#### Key Features
- **Historical Pricing**: `priceAtTime` ensures accurate order totals even if product prices change
- **Stock Validation**: Prevents adding more items than available
- **Automatic Merging**: If variant already in cart, quantity is increased
- **Order Linking**: Cart products are linked to orders during checkout

#### Endpoints
- `GET /api/cart-products` - Get all cart products
- `GET /api/cart-products/{id}` - Get cart product by ID
- `GET /api/cart-products/cart/{cartId}` - Get items by cart ID ⭐
- `POST /api/cart-products` - Add product to cart
- `PUT /api/cart-products` - Update quantity
- `DELETE /api/cart-products/{id}` - Remove from cart

---

### 8. Order Management

#### Overview
Complete order processing system with checkout flow and stock management.

#### Features Implemented

**8.1 Order Operations**
- Create orders manually
- **Create order from cart** (checkout flow) ⭐
- Retrieve orders
- Get orders by user email ⭐
- Update order status
- Delete orders

**8.2 Checkout Flow (from Cart)**
This is the **critical checkout endpoint** that handles the complete order creation process:

1. **Cart Validation**
   - Validates cart exists
   - Checks cart has items
   - Retrieves cart products

2. **Stock Validation**
   - Checks stock for all items
   - Adjusts quantities if stock is insufficient
   - Removes items with zero stock
   - Returns error if all items out of stock

3. **Order Creation**
   - Creates order with shipping details
   - Calculates total price automatically
   - Sets order status (default: pending)
   - Sets payment status (default: pending)
   - Links cart products to order

4. **Stock Updates**
   - Reduces stock quantities for all variants
   - Updates variant timestamps
   - Prevents overselling

5. **Cart Management**
   - Optionally clears cart after order
   - Marks cart as inactive

**8.3 Order Fields**
- Order ID
- User email
- Shipping address
- Phone number
- Total price
- Order status (pending, processing, shipped, delivered, cancelled)
- Payment status (pending, paid, failed, refunded)
- Payment method
- Order date
- Updated timestamp

**8.4 Order Status Flow**
```
pending → processing → shipped → delivered
                ↓
            cancelled
```

**8.5 Payment Status Flow**
```
pending → paid
    ↓
failed / refunded
```

#### Use Cases
- Complete checkout from shopping cart
- Order history for users
- Order status tracking
- Stock management integration
- Historical order data

#### Endpoints
- `GET /api/orders` - Get all orders
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/user/{email}` - Get orders by email ⭐
- `POST /api/orders` - Create order manually
- `POST /api/orders/from-cart/{cartId}` - Create order from cart ⭐ CRITICAL
- `PUT /api/orders/{id}` - Update order
- `DELETE /api/orders/{id}` - Delete order

---

### 9. Image Management

#### Overview
Complete image management system with Cloudinary integration for product and variant images.

#### Features Implemented

**9.1 Image Operations**
- Upload single image
- Upload multiple images
- Create image records (with URL)
- Retrieve images
- Get images by product ID
- Update image information
- Delete images
- Primary image management

**9.2 Image Upload**
- **Single Upload**: Upload one image file
- **Multiple Upload**: Upload multiple images in one request
- Cloudinary integration for storage
- Automatic URL generation
- Support for product and variant images

**9.3 Primary Image Feature**
- Mark image as primary
- Automatic unsetting of other primary images
- One primary image per product

**9.4 Image Fields**
- Image ID
- Product ID (required)
- Variant ID (optional)
- Image URL (Cloudinary URL)
- Primary flag
- Created timestamp

#### Cloudinary Integration
- Automatic file upload to Cloudinary
- URL generation and storage
- Support for various image formats
- Optimized image delivery

#### Endpoints
- `GET /api/images` - Get all images
- `GET /api/images/{id}` - Get image by ID
- `GET /api/images/product/{productId}` - Get images by product
- `POST /api/images/upload` - Upload single image ⭐
- `POST /api/images/upload-multiple` - Upload multiple images ⭐
- `POST /api/images` - Create image record
- `PUT /api/images/{id}` - Update image
- `DELETE /api/images/{id}` - Delete image

---

### 10. Admin Coupon System

#### Overview
Complete coupon management system with validation, discount calculation, and usage tracking.

#### Features Implemented

**10.1 Coupon Operations**
- Create coupons
- Retrieve coupons (all or active only)
- Get coupon by ID or code
- Update coupon information
- Delete coupons
- **Coupon validation** with discount calculation ⭐

**10.2 Coupon Types**
- **Percentage Discount**: Percentage off total (e.g., 20% off)
- **Fixed Discount**: Fixed amount off (e.g., $10 off)

**10.3 Coupon Validation**
The validation endpoint checks:
1. Coupon exists
2. Coupon is active
3. Current date is within valid range
4. Usage limit not exceeded
5. Minimum purchase amount met
6. Calculates discount amount
7. Applies maximum discount cap (for percentage)

**10.4 Discount Calculation**
- **Percentage**: `discount = (cart_total * discount_value / 100)`, capped at `max_discount_amount`
- **Fixed**: `discount = discount_value`, capped at cart total

**10.5 Coupon Fields**
- Coupon ID
- Coupon code (unique, uppercase)
- Description
- Discount type (PERCENTAGE or FIXED)
- Discount value
- Minimum purchase amount
- Maximum discount amount (for percentage)
- Usage limit
- Used count
- Valid from date
- Valid until date
- Active status
- Created/updated timestamps

#### Use Cases
- Promotional campaigns
- Seasonal discounts
- Customer loyalty rewards
- First-time buyer discounts
- Bulk purchase discounts

#### Endpoints
- `GET /api/admin/coupons` - Get all coupons
- `GET /api/admin/coupons/active` - Get active coupons
- `GET /api/admin/coupons/{id}` - Get coupon by ID
- `GET /api/admin/coupons/code/{code}` - Get coupon by code
- `POST /api/admin/coupons` - Create coupon
- `PUT /api/admin/coupons/{id}` - Update coupon
- `DELETE /api/admin/coupons/{id}` - Delete coupon
- `POST /api/admin/coupons/validate/{code}` - Validate coupon ⭐

---

### 11. Admin Activity Logging

#### Overview
Complete audit trail system for tracking all admin actions.

#### Features Implemented

**11.1 Activity Logging**
- Log admin activities
- Quick log endpoint (simplified)
- Full log endpoint (complete object)
- Automatic IP address capture
- Automatic User-Agent capture
- Timestamp tracking

**11.2 Activity Retrieval**
- Get all activities (paginated)
- Get activity by ID
- Get activities by admin email
- Get activities by entity type
- Get activities by action type
- Delete activity logs

**11.3 Action Types**
- CREATE: Creating new entities
- UPDATE: Updating existing entities
- DELETE: Deleting entities
- VIEW: Viewing/accessing entities
- LOGIN: Admin login
- LOGOUT: Admin logout
- APPROVE: Approving something
- REJECT: Rejecting something

**11.4 Entity Types**
- PRODUCT: Product-related actions
- ORDER: Order-related actions
- COUPON: Coupon-related actions
- USER: User/Account-related actions
- CATEGORY: Category-related actions
- SELLER: Seller-related actions
- CART: Cart-related actions
- SYSTEM: System configuration actions

**11.5 Activity Fields**
- Activity ID
- Admin email
- Action type
- Entity type
- Entity ID (optional)
- Description
- IP address (auto-captured)
- User-Agent (auto-captured)
- Created timestamp

#### Use Cases
- Security auditing
- Compliance tracking
- Admin action monitoring
- Troubleshooting
- User activity analysis

#### Endpoints
- `GET /api/admin/activities` - Get all activities (paginated)
- `GET /api/admin/activities/{id}` - Get activity by ID
- `GET /api/admin/activities/admin/{email}` - Get by admin email
- `GET /api/admin/activities/entity/{entityType}` - Get by entity type
- `GET /api/admin/activities/action/{actionType}` - Get by action type
- `POST /api/admin/activities` - Log activity (full object)
- `POST /api/admin/activities/quick-log` - Quick log activity ⭐
- `DELETE /api/admin/activities/{id}` - Delete activity

---

## API Endpoints Summary

### Total Endpoints: 64+

#### By Category

| Category | Endpoints | Description |
|----------|-----------|-------------|
| Authentication | 3 | Register, login, account check |
| Account Management | 5 | CRUD operations for accounts |
| Seller Management | 5 | CRUD operations for sellers |
| Category Management | 5 | CRUD operations for categories |
| Product Management | 5 | CRUD operations for products |
| Variant Management | 6 | Variant CRUD + availability check |
| Cart Management | 8 | Cart CRUD + sync + calculate total |
| Cart Product Management | 6 | Add, update, remove cart items |
| Order Management | 7 | Order CRUD + checkout from cart |
| Image Management | 8 | Image upload + CRUD operations |
| Admin Coupons | 8 | Coupon CRUD + validation |
| Admin Activities | 8 | Activity logging + retrieval |

### Base URL
```
http://localhost:8080/api
```

### Swagger Documentation
```
http://localhost:8080/swagger-ui.html
http://localhost:8080/swagger-ui/index.html
```

---

## Database Schema

### Core Tables

1. **accounts** - User accounts
2. **sellers** - Seller profiles
3. **categories** - Product categories
4. **products** - Product catalog
5. **product_variants** - Product variants (size, color, etc.)
6. **carts** - Shopping carts
7. **cart_products** - Cart items
8. **orders** - Customer orders
9. **images** - Product/variant images
10. **coupons** - Discount coupons
11. **admin_activities** - Activity logs

### Relationships

- Account → Seller (one-to-one, via email)
- Account → Cart (one-to-many, via email)
- Account → Order (one-to-many, via email)
- Category → Product (one-to-many)
- Seller → Product (one-to-many)
- Product → ProductVariant (one-to-many)
- Product → Image (one-to-many)
- ProductVariant → Image (one-to-many)
- Cart → CartProduct (one-to-many)
- Order → CartProduct (one-to-many, via orderId)

### Database Script
Use `database/database_complete.sql` for complete database setup.

---

## Security Features

### Authentication
- **JWT Tokens**: Token-based authentication
- **Password Hashing**: BCrypt with salt
- **Token Expiry**: 24 hours
- **Account Status**: Active/inactive validation

### CORS Configuration
- Enabled for all origins (`*`)
- Applied to `/api/**` endpoints

### Security Configuration
- Spring Security integration
- JWT filter chain
- Password encoder configuration

### Recommended Enhancements
- Role-based access control (RBAC)
- Admin endpoint protection
- Rate limiting
- Input validation and sanitization

---

## Integration Guide

### Frontend Integration

#### 1. Authentication Flow
```javascript
// Register
POST /api/auth/register
{
  "email": "user@example.com",
  "password": "password123",
  "first_name": "John",
  "last_name": "Doe",
  "seller_account": false
}

// Login
POST /api/auth/login
{
  "email": "user@example.com",
  "password": "password123"
}

// Use token in subsequent requests
Authorization: Bearer <token>
```

#### 2. Shopping Cart Flow
```javascript
// Sync cart
POST /api/carts/sync
{
  "email": "user@example.com"
}

// Add to cart
POST /api/cart-products
{
  "variant_id": 123,
  "cart_id": 456,
  "quantity": 2
}

// Get cart items
GET /api/cart-products/cart/{cartId}

// Calculate total
PUT /api/carts/{cartId}/calculate-total
```

#### 3. Checkout Flow
```javascript
// Create order from cart
POST /api/orders/from-cart/{cartId}
{
  "address": "123 Main St",
  "phone_num": "1234567890",
  "payment_method": "credit_card",
  "clear_cart": true
}
```

#### 4. Coupon Validation
```javascript
// Validate coupon
POST /api/admin/coupons/validate/SAVE20
{
  "cart_total": 150.00
}

// Response
{
  "valid": true,
  "coupon_code": "SAVE20",
  "discount": 30.00,
  "final_amount": 120.00
}
```

### Error Handling

All endpoints return appropriate HTTP status codes:
- `200 OK`: Success
- `201 Created`: Resource created
- `400 Bad Request`: Validation error
- `401 Unauthorized`: Authentication error
- `404 Not Found`: Resource not found
- `500 Internal Server Error`: Server error

Error responses include error messages:
```json
{
  "error": "Error message here"
}
```

---

## Deployment

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL database
- Cloudinary account (for image uploads)

### Configuration

Edit `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/victusstore
spring.datasource.username=your_username
spring.datasource.password=your_password

# Server
server.port=8080

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# Cloudinary (if using image upload)
cloudinary.cloud_name=your_cloud_name
cloudinary.api_key=your_api_key
cloudinary.api_secret=your_api_secret
```

### Build and Run

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Or using Maven wrapper
./mvnw spring-boot:run  # Linux/Mac
.\mvnw.cmd spring-boot:run  # Windows
```

### Docker Deployment

A `Dockerfile` is included for containerized deployment.

---

## Testing

### Manual Testing
Use Swagger UI at `http://localhost:8080/swagger-ui.html` for interactive API testing.

### Test Checklist

#### Authentication
- [ ] Register new user
- [ ] Login with credentials
- [ ] Verify JWT token generation
- [ ] Check account verification

#### Products
- [ ] Create product
- [ ] Get products with pagination
- [ ] Update product
- [ ] Delete product

#### Cart
- [ ] Add product to cart
- [ ] Sync cart
- [ ] Calculate cart total
- [ ] Update quantity
- [ ] Remove from cart

#### Orders
- [ ] Create order from cart
- [ ] Verify stock updates
- [ ] Get orders by email
- [ ] Update order status

#### Coupons
- [ ] Create coupon
- [ ] Validate coupon
- [ ] Test percentage discount
- [ ] Test fixed discount
- [ ] Test expired coupon

#### Admin Activities
- [ ] Log activity
- [ ] Get activities by admin
- [ ] Get activities by entity type

---

## Future Enhancements

### Recommended Features
1. **Payment Integration**: Payment gateway integration
2. **Email Notifications**: Order confirmation emails
3. **Inventory Alerts**: Low stock notifications
4. **Advanced Search**: Product search with filters
5. **Reviews & Ratings**: Customer review system
6. **Wishlist**: User wishlist functionality
7. **Multi-currency**: Support for multiple currencies
8. **Analytics**: Sales and product analytics
9. **Bulk Operations**: Bulk product/category updates
10. **API Rate Limiting**: Prevent abuse

### Security Enhancements
1. **Role-Based Access Control**: Admin, seller, customer roles
2. **API Rate Limiting**: Prevent DDoS attacks
3. **Input Sanitization**: XSS prevention
4. **SQL Injection Prevention**: Parameterized queries (already implemented)
5. **HTTPS Enforcement**: SSL/TLS configuration

---

## Support & Documentation

### Documentation Files
- `README.md` - Project overview and quick start
- `docs/API_ENDPOINTS_DOCUMENTATION.md` - Complete API reference
- `docs/NEW_API_FEATURES_DOCUMENTATION.md` - Latest features
- `docs/FEATURES_DOCUMENTATION.md` - This file
- `docs/PROJECT_STRUCTURE.md` - Project organization

### Database Scripts
- `database/database_complete.sql` - Complete database setup ⭐ USE THIS

### API Documentation
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Summary

VictusStore is a production-ready e-commerce backend with:

✅ **64+ API Endpoints** covering all e-commerce operations  
✅ **Complete Checkout Flow** from cart to order  
✅ **Stock Management** with automatic updates  
✅ **Coupon System** with validation and discount calculation  
✅ **Admin Activity Logging** for audit trails  
✅ **Image Management** with Cloudinary integration  
✅ **JWT Authentication** for secure access  
✅ **RESTful API Design** following best practices  
✅ **Comprehensive Documentation** for easy integration  

The system is designed to be scalable, maintainable, and easy to integrate with any frontend framework.

---

**Last Updated**: Latest  
**Version**: 2.0  
**Status**: Production Ready

