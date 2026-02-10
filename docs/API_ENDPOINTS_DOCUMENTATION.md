# VictusStore API - Complete Endpoints Documentation

## Base URL
```
http://localhost:8080/api
```

---

## System Overview

VictusStore is a lightweight, production-oriented e-commerce backend implemented with Spring Boot and JPA. It focuses on reliability, clear API semantics, and real-world checkout flows. Strong points:

- **Complete checkout flow**: End-to-end cart → order pipeline with stock validation, price calculations, and optional cart clearing to support reliable checkout.
- **Robust cart model**: Cart products store the `priceAtTime` to preserve historical pricing and allow accurate order totals even if product prices change later.
- **Variant-aware pricing**: Product variants and base product pricing are combined at add-to-cart time so totals reflect both base price and variant adjustments.
- **Defensive validation**: Controllers validate input (presence, quantities, and stock) and return clear error responses for common issues (missing fields, out-of-stock, not-found resources).
- **Extensible image handling**: Image upload endpoints support single and multiple file uploads and store image metadata for products and variants.
- **Admin and coupon features**: Built-in admin activity logging and coupon validation allow auditing and flexible discounting workflows.
- **CORS-enabled, JWT-ready**: APIs are CORS-friendly for frontend integration and include authentication endpoints (JWT) ready to protect routes.
- **Pragmatic API design**: Endpoints follow RESTful conventions, provide helpful response payloads, and include convenience endpoints (get cart by email, get orders by email).

---

## Endpoint Catalog (Summary)

This section gives a **compact, professional overview of every HTTP endpoint** in the backend.  
Detailed request/response examples for most endpoints are available in the sections that follow.

### Legend

- **Auth**: ✅ = JWT access token required, ❌ = Public  
- **Role**: CUSTOMER, SELLER, ADMIN, or ❌ = No specific role required  
- **Rate Limit**: ✅ = Rate limited (see [Rate Limiting Details](#rate-limiting-details))  
- **Idempotent**: ✅ = Supports `Idempotency-Key` header

---

### Authentication (`/api/auth`)

| Method | Endpoint                                  | Auth | Role | Rate Limit | Notes / Response |
|--------|-------------------------------------------|------|------|------------|------------------|
| POST   | `/api/auth/register`                      | ❌   | ❌   | ✅ (3/min) | Registers account, returns `{access_token, refresh_token, email, role}` |
| POST   | `/api/auth/login`                         | ❌   | ❌   | ✅ (5/min) | Authenticates user, returns `{access_token, refresh_token, email, role}` |
| POST   | `/api/auth/refresh`                       | ❌   | ❌   | ❌         | Refreshes tokens using `refresh_token` |
| GET    | `/api/auth/check_account/{email}/{password}` | ❌ | ❌   | ❌         | Quick password and existence check |
| POST   | `/api/auth/change-password`               | ✅   | Any | ❌         | Change password for current authenticated user |

---

### Health & Monitoring

| Method | Endpoint        | Auth | Role | Rate Limit | Notes / Response |
|--------|-----------------|------|------|------------|------------------|
| GET    | `/api/health`   | ❌   | ❌   | ❌         | Health probe: `{status, application, version, uptime, database}` |

---

### Account Management (`/api/accounts`)

| Method | Endpoint                    | Auth | Role | Rate Limit | Notes / Response |
|--------|-----------------------------|------|------|------------|------------------|
| GET    | `/api/accounts`             | ✅   | ❌   | ❌         | List all accounts |
| GET    | `/api/accounts/{email}`     | ✅   | ❌   | ❌         | Get account by email |
| POST   | `/api/accounts`             | ✅   | ❌   | ❌         | Create account (password hashed) |
| PUT    | `/api/accounts/{email}`     | ✅   | ❌   | ❌         | Update account |
| DELETE | `/api/accounts/{email}`     | ✅   | ❌   | ❌         | Delete account |

---

### Seller Management (`/api/sellers`)

| Method | Endpoint                | Auth | Role | Rate Limit | Notes / Response |
|--------|-------------------------|------|------|------------|------------------|
| GET    | `/api/sellers`          | ✅   | ❌   | ❌         | List all sellers |
| GET    | `/api/sellers/{id}`     | ✅   | ❌   | ❌         | Get seller by ID |
| POST   | `/api/sellers`          | ✅   | ❌   | ❌         | Create seller |
| PUT    | `/api/sellers/{id}`     | ✅   | ❌   | ❌         | Update seller |
| DELETE | `/api/sellers/{id}`     | ✅   | ❌   | ❌         | Delete seller |

---

### Category Management (`/api/categories`)

| Method | Endpoint                    | Auth | Role | Rate Limit | Notes / Response |
|--------|-----------------------------|------|------|------------|------------------|
| GET    | `/api/categories`           | ✅   | ❌   | ❌         | List categories |
| GET    | `/api/categories/{id}`      | ✅   | ❌   | ❌         | Get category by ID |
| POST   | `/api/categories`           | ✅   | ❌   | ❌         | Create category |
| PUT    | `/api/categories/{id}`      | ✅   | ❌   | ❌         | Update category |
| DELETE | `/api/categories/{id}`      | ✅   | ❌   | ❌         | Delete category |

---

### Product Management (`/api/products`)

| Method | Endpoint                                     | Auth | Role | Rate Limit | Notes / Response |
|--------|----------------------------------------------|------|------|------------|------------------|
| GET    | `/api/products`                              | ✅   | ❌   | ❌         | Paginated products (`page`, `size`) |
| GET    | `/api/products/{id}`                         | ✅   | ❌   | ❌         | Get product by ID |
| GET    | `/api/products/category/{categoryId}`        | ✅   | ❌   | ❌         | Get products for a category |
| POST   | `/api/products`                              | ✅   | ❌   | ❌         | Create product |
| PUT    | `/api/products/{id}`                         | ✅   | ❌   | ❌         | Update product |
| DELETE | `/api/products/{id}`                         | ✅   | ❌   | ❌         | Delete product |

---

### Product Variant Management (`/api/variants`)

| Method | Endpoint                                     | Auth | Role | Rate Limit | Notes / Response |
|--------|----------------------------------------------|------|------|------------|------------------|
| GET    | `/api/variants`                              | ✅   | ❌   | ❌         | List all variants |
| GET    | `/api/variants/{id}`                         | ✅   | ❌   | ❌         | Get variant by ID |
| GET    | `/api/variants/{id}/check-availability`      | ✅   | ❌   | ❌         | Stock availability view |
| GET    | `/api/variants/product/{productId}`          | ✅   | ❌   | ❌         | Variants for given product |
| POST   | `/api/variants`                              | ✅   | ❌   | ❌         | Create variant |
| PUT    | `/api/variants/{id}`                         | ✅   | ❌   | ❌         | Update variant |

---

### Cart Management (`/api/carts`)

| Method | Endpoint                              | Auth | Role | Rate Limit | Notes / Response |
|--------|---------------------------------------|------|------|------------|------------------|
| GET    | `/api/carts`                          | ✅   | ❌   | ❌         | List carts |
| GET    | `/api/carts/{id}`                     | ✅   | ❌   | ❌         | Get cart by ID |
| GET    | `/api/carts/user/{email}`             | ✅   | ❌   | ❌         | Get cart by user email |
| POST   | `/api/carts`                          | ✅   | ❌   | ❌         | Create cart |
| POST   | `/api/carts/sync`                     | ✅   | ❌   | ❌         | Sync cart by email |
| PUT    | `/api/carts/{id}`                     | ✅   | ❌   | ❌         | Update cart fields |
| PUT    | `/api/carts/{id}/calculate-total`     | ✅   | ❌   | ❌         | Recalculate total |
| DELETE | `/api/carts/{id}`                     | ✅   | ❌   | ❌         | Delete cart |

---

### Cart Product Management (`/api/cart-products`)

| Method | Endpoint                                 | Auth | Role | Rate Limit | Notes / Response |
|--------|------------------------------------------|------|------|------------|------------------|
| GET    | `/api/cart-products`                     | ✅   | ❌   | ❌         | List all cart items |
| GET    | `/api/cart-products/{id}`                | ✅   | ❌   | ❌         | Get cart item by ID |
| GET    | `/api/cart-products/cart/{cartId}`       | ✅   | ❌   | ❌         | Items for a cart |
| POST   | `/api/cart-products`                     | ✅   | ❌   | ❌         | Add variant to cart |
| PUT    | `/api/cart-products`                     | ✅   | ❌   | ❌         | Update quantity |
| DELETE | `/api/cart-products/{id}`                | ✅   | ❌   | ❌         | Remove item from cart |

---

### Order Management (`/api/orders`)

| Method | Endpoint                                  | Auth | Role | Rate Limit | Idempotent | Notes / Response |
|--------|-------------------------------------------|------|------|------------|-----------|------------------|
| GET    | `/api/orders`                             | ✅   | ❌   | ❌         | ❌        | List all orders |
| GET    | `/api/orders/with-products`               | ✅   | ❌   | ❌         | ❌        | Orders with product details |
| GET    | `/api/orders/{id}`                        | ✅   | ❌   | ❌         | ❌        | Get order by ID |
| GET    | `/api/orders/user/{email}`                | ✅   | ❌   | ❌         | ❌        | Orders for a user |
| POST   | `/api/orders`                             | ✅   | ❌   | ❌         | ❌        | Create order manually |
| POST   | `/api/orders/from-cart/{cartId}`          | ✅   | ❌   | ✅ (10/min) | ✅       | Checkout from cart (stock + totals) |
| PUT    | `/api/orders/{id}`                        | ✅   | ❌   | ❌         | ❌        | Update order |
| DELETE | `/api/orders/{id}`                        | ✅   | ❌   | ❌         | ❌        | Delete order |

---

### Image Management (`/api/images`)

| Method | Endpoint                                 | Auth | Role | Rate Limit | Notes / Response |
|--------|------------------------------------------|------|------|------------|------------------|
| GET    | `/api/images`                            | ✅   | ❌   | ❌         | List all images |
| GET    | `/api/images/{id}`                       | ✅   | ❌   | ❌         | Get image by ID |
| GET    | `/api/images/product/{productId}`        | ✅   | ❌   | ❌         | Images for a product |
| POST   | `/api/images/upload`                     | ✅   | ❌   | ❌         | Upload single image (`multipart/form-data`) |
| POST   | `/api/images/upload-multiple`            | ✅   | ❌   | ❌         | Upload multiple images |
| POST   | `/api/images`                            | ✅   | ❌   | ❌         | Create image record (URL-based) |
| PUT    | `/api/images/{id}`                       | ✅   | ❌   | ❌         | Update image |
| DELETE | `/api/images/{id}`                       | ✅   | ❌   | ❌         | Delete image |

---

### Admin Coupons (`/api/admin/coupons`)

| Method | Endpoint                                   | Auth | Role | Rate Limit | Notes / Response |
|--------|--------------------------------------------|------|------|------------|------------------|
| GET    | `/api/admin/coupons`                       | ✅   | ADMIN | ❌        | List all coupons |
| GET    | `/api/admin/coupons/active`                | ✅   | ADMIN | ❌        | List active coupons |
| GET    | `/api/admin/coupons/{id}`                  | ✅   | ADMIN | ❌        | Get coupon by ID |
| GET    | `/api/admin/coupons/code/{code}`           | ✅   | ADMIN | ❌        | Get coupon by code |
| POST   | `/api/admin/coupons`                       | ✅   | ADMIN | ❌        | Create coupon |
| PUT    | `/api/admin/coupons/{id}`                  | ✅   | ADMIN | ❌        | Update coupon |
| DELETE | `/api/admin/coupons/{id}`                  | ✅   | ADMIN | ❌        | Delete coupon |
| POST   | `/api/admin/coupons/validate/{code}`       | ✅   | ADMIN | ✅ (20/min) | Validate coupon & compute discount |

---

### Admin Activity Logging (`/api/admin/activities`)

| Method | Endpoint                                          | Auth | Role | Rate Limit | Notes / Response |
|--------|---------------------------------------------------|------|------|------------|------------------|
| GET    | `/api/admin/activities`                           | ✅   | ADMIN | ❌       | Paginated activities (`page`, `size`) |
| GET    | `/api/admin/activities/{id}`                      | ✅   | ADMIN | ❌       | Get activity by ID |
| GET    | `/api/admin/activities/admin/{email}`             | ✅   | ADMIN | ❌       | Activities per admin (paginated) |
| GET    | `/api/admin/activities/entity/{entityType}`       | ✅   | ADMIN | ❌       | Activities per entity type |
| GET    | `/api/admin/activities/action/{actionType}`       | ✅   | ADMIN | ❌       | Activities per action type |
| POST   | `/api/admin/activities`                           | ✅   | ADMIN | ❌       | Log activity (full object) |
| POST   | `/api/admin/activities/quick-log`                 | ✅   | ADMIN | ❌       | Quick log activity (simplified DTO) |
| DELETE | `/api/admin/activities/{id}`                      | ✅   | ADMIN | ❌       | Delete activity |

---

### Store Settings (`/api/admin/settings`)

| Method | Endpoint                   | Auth | Role  | Rate Limit | Notes / Response |
|--------|----------------------------|------|-------|------------|------------------|
| GET    | `/api/admin/settings`      | ✅   | ADMIN | ❌        | Get current store configuration (name, email, phone, address) |
| PUT    | `/api/admin/settings`      | ✅   | ADMIN | ❌        | Update persisted store configuration |


## Authentication & Security (`/api/auth`, JWT, Errors)

### 1. Register User (`POST /api/auth/register`)
- **Endpoint**: `POST /api/auth/register`
- **Description**: Creates a new user account. If `seller_account` is true, also creates a seller profile and returns a `seller_id`.
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "mypassword123",
    "first_name": "John",
    "last_name": "Doe",
    "phone_num": "+1 (555) 123-4567",
    "seller_account": true
  }
  ```
- **Validation Rules**:
  - `email`: required, non-blank, normalized to lowercase, must not already exist.
  - `password`: required, **minimum 8 characters**.
  - `phone_num` (optional):
    - All non-digits are stripped.
    - If more than 10 digits, only the last 10 are used.
    - If less than 10 digits after cleaning → `400 Bad Request` with:
      ```json
      { "error": "Phone number must contain at least 10 digits" }
      ```
- **Success Response** (200 OK):
  ```json
  {
    "message": "Account created successfully",
    "email": "user@example.com",
    "access_token": "<JWT access token>",
    "refresh_token": "<JWT refresh token>",
    "role": "SELLER",
    "seller_id": 123
  }
  ```
  - `role` is `"SELLER"` when `seller_account` is true, otherwise `"CUSTOMER"`.
  - `seller_id` is only present when `seller_account` is true.
- **Error Responses**:
  - `400 Bad Request`:
    - `{ "error": "Email is required" }`
    - `{ "error": "Password must be at least 8 characters long" }`
    - `{ "error": "Phone number must contain at least 10 digits" }`
    - `{ "error": "Email already exists" }`
  - `500 Internal Server Error`:
    - `{ "error": "Internal server error" }`

---

### 2. Login User (`POST /api/auth/login`)
- **Endpoint**: `POST /api/auth/login`
- **Description**: Authenticates user and returns **access** and **refresh** JWT tokens plus role/seller flags.
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "mypassword123"
  }
  ```
- **Validation Rules**:
  - Body must contain both `email` and `password`, otherwise:
    ```json
    { "error": "Email and password are required" }
    ```
    (`400 Bad Request`)
  - Email must contain `@`, otherwise:
    ```json
    { "error": "Invalid email format" }
    ```
    (`400 Bad Request`)
- **Success Response** (200 OK):
  ```json
  {
    "message": "Login successful",
    "access_token": "<JWT access token>",
    "refresh_token": "<JWT refresh token>",
    "email": "user@example.com",
    "role": "CUSTOMER",
    "seller_account": false
  }
  ```
  - `role` is taken from the account’s stored role if present, otherwise inferred:
    - `"SELLER"` when `seller_account` is true, else `"CUSTOMER"`.
- **Error Responses**:
  - `401 Unauthorized`:
    ```json
    { "error": "Invalid credentials" }
    ```
    (account not found or password mismatch)
  - `401 Unauthorized`:
    ```json
    { "error": "Account is deactivated" }
    ```
  - `500 Internal Server Error`:
    ```json
    { "error": "Internal server error" }
    ```

---

### 3. Refresh Token (`POST /api/auth/refresh`)
- **Endpoint**: `POST /api/auth/refresh`
- **Description**: Exchanges a valid refresh token for a new access token and a rotated refresh token.
- **Request Body**:
  ```json
  {
    "refresh_token": "<refresh token>"
  }
  ```
- **Success Response** (200 OK):
  ```json
  {
    "access_token": "<new access token>",
    "refresh_token": "<new refresh token>",
    "email": "user@example.com",
    "role": "CUSTOMER"
  }
  ```
- **Error Responses**:
  - `400 Bad Request`:
    ```json
    { "error": "Refresh token is required" }
    ```
  - `401 Unauthorized`:
    ```json
    { "error": "Refresh token not found" }
    ```
    or:
    ```json
    { "error": "Refresh token has expired" }
    ```
    or:
    ```json
    { "error": "Refresh token has been revoked" }
    ```
  - `401 Unauthorized`:
    ```json
    { "error": "Account is deactivated" }
    ```
  - `500 Internal Server Error`:
    ```json
    { "error": "Internal server error" }
    ```

---

### 3a. Change Current User Password (`POST /api/auth/change-password`)
- **Endpoint**: `POST /api/auth/change-password`
- **Auth**: `Authorization: Bearer <access_token>` (any authenticated user)
- **Description**: Changes the password of the **currently authenticated user** by verifying the existing password and applying the same password policy as registration.
- **Request Headers**:
  - `Authorization: Bearer <access_token>`
- **Request Body**:
  ```json
  {
    "currentPassword": "OldPass123!",
    "newPassword": "NewStrongPass123!"
  }
  ```
- **Validation Rules**:
  - `currentPassword`: required; must match the user’s current stored password.
  - `newPassword`: required; must be **at least 8 characters long** (same as registration policy).
- **Success Response** (200 OK):
  ```json
  {
    "message": "Password updated successfully"
  }
  ```
- **Error Responses**:
  - `400 Bad Request`:
    - `{ "error": "currentPassword is required" }`
    - `{ "error": "New password must be at least 8 characters long" }`
  - `401 Unauthorized`:
    - `{ "error": "Unauthorized" }` (missing/invalid token)
  - `403 Forbidden`:
    - `{ "error": "Current password is incorrect" }`
  - `500 Internal Server Error`:
    - `{ "error": "Internal server error" }`

---

### 4. Check Account (`GET /api/auth/check_account/{email}/{password}`)
- **Endpoint**: `GET /api/auth/check_account/{email}/{password}`
- **Description**: Verifies if account exists and password matches.
- **Path Parameters**:
  - `email`: string (required)
  - `password`: string (required)
- **Response (Valid / match)** (200 OK):
  ```json
  {
    "password": true,
    "exists": true,
    "is_seller": true,
    "role": "SELLER"
  }
  ```
- **Response (Not Found)**:
  ```json
  {
    "exists": false
  }
  ```
- **Response (Invalid Password)**:
  ```json
  {
    "password": false,
    "exists": true
  }
  ```
- **Error Response**:
  - `400 Bad Request`:
    ```json
    { "error": "<error message>" }
    ```

---

### 5. JWT Tokens & Lifetimes
- **Access Token**:
  - Field name in responses: `access_token`.
  - Lifetime: **15 minutes**.
  - Claims:
    - `sub`: user email.
    - `role`: `"CUSTOMER" | "SELLER" | "ADMIN"`.
- **Refresh Token**:
  - Field name in responses: `refresh_token`.
  - Lifetime: **7 days** (configurable via `app.refresh-token.expiration-days`, default 7).
  - Contains claim: `"type": "refresh"`.
- **Transport**:
  - Sent by clients in header:
    ```http
    Authorization: Bearer <access_token>
    ```
  - There is **no cookie-based JWT transport**; no `Set-Cookie` for tokens and no server-side cookie reading for auth.

---

### 6. Authentication Expectations for Protected Endpoints
- All non-public endpoints (everything except:
  - `/api/auth/**`
  - `/api/health`
  - `/actuator/health`
  - `GET /api/products/**`
  - `GET /api/categories/**`
  - `GET /api/variants/**`
  - `GET /api/images/**`
  ) require:
  - `Authorization: Bearer <access_token>` header.
- No cookies are required or checked for authentication.

---

### 7. Global Error Format & Common Codes
- Many controllers (especially order/stock/idempotency) use a structured error envelope:
  ```json
  {
    "error": {
      "code": "SOME_CODE",
      "message": "User friendly message",
      "details": {
        "field": "extra info"
      },
      "traceId": "abc123"
    }
  }
  ```
- Common `error.code` values:
  - `INVALID_ARGUMENT` (400) – e.g. `"Cart not found"`, `"Cart is empty"`, `"Account not found"`.
  - `VALIDATION_ERROR` (400) – DTO validation failures; `details` has `{ "field": "error message" }`.
  - `CONSTRAINT_VIOLATION` (400) – constraint violations.
  - `ACCESS_DENIED` (403) – missing permissions/roles.
  - `INVALID_CREDENTIALS` (401) – Spring Security authentication failures.
  - `STOCK_INSUFFICIENT` (400) – checkout stock problems, with `details.variant_id`, `details.available_stock`, `details.requested_quantity`.
  - `IDEMPOTENCY_KEY_REUSE_MISMATCH` (409) – same `Idempotency-Key` reused with a different body.
  - `INTERNAL_ERROR` (500) – unexpected errors.
- **Rate limiting** uses a simpler envelope:
  ```json
  {
    "error": {
      "code": "RATE_LIMIT_EXCEEDED",
      "message": "Too many login attempts. Please try again later."
    }
  }
  ```
  - Status: `429 Too Many Requests`.

---

### 8. CORS & Deployment Expectations
- **Allowed Origins** (configurable in `application.properties`):
  - `https://victusstore.store`
  - `https://www.victusstore.store`
  - `http://localhost:5173`
  - `http://localhost:3000`
- **Methods**: `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS`, `PATCH`.
- **Headers**:
  - Allowed: `*` (all).
  - Exposed to frontend: `X-Trace-Id`.
- **Credentials**:
  - `allowCredentials=true` (CORS-level) but no auth cookies are used; JWTs are header-only.
- **HTTPS**:
  - App runs on HTTP (`:8080`) and is expected to be fronted by a reverse proxy (e.g. Nginx / load balancer) that terminates TLS.

---

## Account Management (`/api/accounts`)

### 4. Get All Accounts
- **Endpoint**: `GET /api/accounts`
- **Description**: Retrieves all user accounts.
- **Parameters**: None
- **Response**: Array of Account objects
  ```json
  [
    {
      "email": "string",
      "firstName": "string",
      "lastName": "string",
      "phoneNum": "string",
      "sellerAccount": true/false,
      "createdAt": "timestamp",
      "lastLogin": "timestamp",
      "isActive": true/false
    }
  ]
  ```

---

### 5. Get Account by Email
- **Endpoint**: `GET /api/accounts/{email}`
- **Description**: Retrieves a specific account by email.
- **Path Parameters**:
  - `email`: string (required)
- **Response**: Account object
- **Error Response**: `404 Not Found` if account doesn't exist

---

### 6. Create Account
- **Endpoint**: `POST /api/accounts`
- **Description**: Creates a new account (password is automatically hashed).
- **Request Body**: Account object
  ```json
  {
    "email": "string (required)",
    "password": "string (required, will be hashed)",
    "firstName": "string",
    "lastName": "string",
    "phoneNum": "string",
    "sellerAccount": true/false,
    "isActive": true/false
  }
  ```
- **Response**: Created Account object

---

### 7. Update Account
- **Endpoint**: `PUT /api/accounts/{email}`
- **Description**: Updates an existing account.
- **Path Parameters**:
  - `email`: string (required)
- **Request Body**: Account object with updated fields
  ```json
  {
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "phoneNum": "string",
    "password": "string (optional - will be hashed if provided)",
    "sellerAccount": true/false,
    "isActive": true/false
  }
  ```
- **Response**: Updated Account object
- **Error Response**: `404 Not Found` if account doesn't exist

---

### 8. Delete Account
- **Endpoint**: `DELETE /api/accounts/{email}`
- **Description**: Deletes an account.
- **Path Parameters**:
  - `email`: string (required)
- **Response**:
  ```json
  {
    "deleted": true
  }
  ```
- **Error Response**: `404 Not Found` if account doesn't exist

---

## Seller Management (`/api/sellers`)

### 9. Get All Sellers
- **Endpoint**: `GET /api/sellers`
- **Description**: Retrieves all seller profiles.
- **Parameters**: None
- **Response**: Array of Seller objects
  ```json
  [
    {
      "sellerId": 123,
      "sellerName": "string",
      "email": "string",
      "rating": 4.5,
      "createdAt": "timestamp",
      "isActive": true/false
    }
  ]
  ```

---

### 10. Get Seller by ID
- **Endpoint**: `GET /api/sellers/{id}`
- **Description**: Retrieves a specific seller by ID.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**: Seller object
- **Error Response**: `404 Not Found` if seller doesn't exist

---

### 11. Create Seller
- **Endpoint**: `POST /api/sellers`
- **Description**: Creates a new seller profile.
- **Request Body**: Seller object
  ```json
  {
    "sellerName": "string (required)",
    "email": "string (required)",
    "rating": 0.0,
    "isActive": true/false
  }
  ```
- **Response**: Created Seller object

---

### 12. Update Seller
- **Endpoint**: `PUT /api/sellers/{id}`
- **Description**: Updates seller information.
- **Path Parameters**:
  - `id`: Long (required)
- **Request Body**: Seller object with updated fields
  ```json
  {
    "sellerName": "string",
    "rating": 4.5,
    "isActive": true/false
  }
  ```
- **Response**: Updated Seller object
- **Error Response**: `404 Not Found` if seller doesn't exist

---

### 13. Delete Seller
- **Endpoint**: `DELETE /api/sellers/{id}`
- **Description**: Deletes a seller profile.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**:
  ```json
  {
    "deleted": true
  }
  ```
- **Error Response**: `404 Not Found` if seller doesn't exist

---

## Category Management (`/api/categories`)

### 14. Get All Categories
- **Endpoint**: `GET /api/categories`
- **Description**: Retrieves all product categories.
- **Parameters**: None
- **Response**: Array of Category objects
  ```json
  [
    {
      "categoryId": 123,
      "categoryName": "string",
      "categoryImage": "string (URL)",
      "parentCategoryId": 456 (optional),
      "isActive": true/false,
      "createdAt": "timestamp"
    }
  ]
  ```

---

### 15. Get Category by ID
- **Endpoint**: `GET /api/categories/{id}`
- **Description**: Retrieves a specific category by ID.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**: Category object
- **Error Response**: `404 Not Found` if category doesn't exist

---

### 16. Create Category
- **Endpoint**: `POST /api/categories`
- **Description**: Creates a new product category.
- **Request Body**: Category object
  ```json
  {
    "categoryName": "string (required, unique)",
    "categoryImage": "string (URL, optional)",
    "parentCategoryId": 456 (optional, for hierarchy),
    "isActive": true/false
  }
  ```
- **Response**: Created Category object

---

### 17. Update Category
- **Endpoint**: `PUT /api/categories/{id}`
- **Description**: Updates category information.
- **Path Parameters**:
  - `id`: Long (required)
- **Request Body**: Category object with updated fields
  ```json
  {
    "categoryName": "string",
    "categoryImage": "string (URL)",
    "isActive": true/false
  }
  ```
- **Response**: Updated Category object
- **Error Response**: `404 Not Found` if category doesn't exist

---

### 18. Delete Category
- **Endpoint**: `DELETE /api/categories/{id}`
- **Description**: Deletes a category.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**:
  ```json
  {
    "deleted": true
  }
  ```
- **Error Response**: `404 Not Found` if category doesn't exist

---

## Product Management (`/api/products`)

### 19. Get All Products (Paginated)
- **Endpoint**: `GET /api/products`
- **Description**: Retrieves all products with pagination.
- **Query Parameters**:
  - `page`: integer (optional, default: 0)
  - `size`: integer (optional, default: 10)
- **Response**: Paginated Product objects
  ```json
  {
    "content": [
      {
        "productId": 123,
        "productName": "string",
        "description": "string",
        "basePrice": 99.99,
        "categoryId": 456,
        "sellerId": 789,
        "productRating": 4.5,
        "isActive": true/false,
        "createdAt": "timestamp",
        "updatedAt": "timestamp"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "size": 10,
    "number": 0
  }
  ```

---

### 20. Get Product by ID
- **Endpoint**: `GET /api/products/{id}`
- **Description**: Retrieves a specific product by ID.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**: Product object (includes category, seller, variants, images relations)
- **Error Response**: `404 Not Found` if product doesn't exist

---

### 21. Create Product
- **Endpoint**: `POST /api/products`
- **Description**: Creates a new product.
- **Request Body**: Product object
  ```json
  {
    "productName": "string (required)",
    "description": "string (optional)",
    "basePrice": 99.99 (required, decimal),
    "categoryId": 456 (optional),
    "sellerId": 789 (optional),
    "productRating": 0.0,
    "isActive": true/false
  }
  ```
- **Response**: Created Product object

---

### 22. Update Product
- **Endpoint**: `PUT /api/products/{id}`
- **Description**: Updates product information (only updates provided fields).
- **Path Parameters**:
  - `id`: Long (required)
- **Request Body**: Product object with fields to update
  ```json
  {
    "productName": "string (optional)",
    "description": "string (optional)",
    "basePrice": 99.99 (optional),
    "isActive": true/false (optional)
  }
  ```
- **Response**: Updated Product object
- **Error Response**: `404 Not Found` if product doesn't exist

---

### 23. Delete Product
- **Endpoint**: `DELETE /api/products/{id}`
- **Description**: Deletes a product.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**:
  ```json
  {
    "deleted": true
  }
  ```
- **Error Response**: `404 Not Found` if product doesn't exist

---

## Product Variant Management (`/api/variants`)

### 24. Get All Variants
- **Endpoint**: `GET /api/variants`
- **Description**: Retrieves all product variants.
- **Parameters**: None
- **Response**: Array of ProductVariant objects
  ```json
  [
    {
      "variantId": 123,
      "productId": 456,
      "color": "string",
      "size": "string",
      "stockQuantity": 100,
      "price": 99.99,
      "sku": "string (optional, unique)",
      "isActive": true/false,
      "createdAt": "timestamp",
      "updatedAt": "timestamp"
    }
  ]
  ```

---

### 25. Get Variant by ID
- **Endpoint**: `GET /api/variants/{id}`
- **Description**: Retrieves a specific variant by ID.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**: ProductVariant object
- **Error Response**: 
  ```json
  {
    "message": "Variant not found"
  }
  ```
  Status: `404 Not Found`

---

### 26. Check Variant Availability
- **Endpoint**: `GET /api/variants/{id}/check-availability`
- **Description**: Checks stock availability for a variant.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**:
  ```json
  {
    "variant_id": 123,
    "color": "Red",
    "size": "Large",
    "stock_quantity": 50,
    "product_name": "Product Name"
  }
  ```
- **Error Response**: 
  - `404 Not Found`: "Variant not found"
  - `400 Bad Request`: Error message

---

### 27. Get Variants by Product ID
- **Endpoint**: `GET /api/variants/product/{productId}`
- **Description**: Retrieves all variants for a specific product.
- **Path Parameters**:
  - `productId`: Long (required)
- **Response**: Array of ProductVariant objects

---

### 27a. Create Variant (NEW)
- **Endpoint**: `POST /api/variants`
- **Description**: Creates a new product variant for a given product. The request body should include `productId`, `color`, `size`, `stockQuantity`, `price`, and optionally `sku` and `isActive`.
- **Request Body**:
  ```json
  {
    "productId": 456,
    "color": "Red",
    "size": "M",
    "stockQuantity": 100,
    "price": 9.99,
    "sku": "SKU-RED-M",
    "isActive": true
  }
  ```
- **Response**: Created `ProductVariant` object (Status: `201 Created`)
- **Error Responses**: `400 Bad Request` with error message

---

### 27b. Update Variant (NEW)
- **Endpoint**: `PUT /api/variants/{id}`
- **Description**: Updates an existing variant's fields. Only the fields on the request body will be applied to the stored variant.
- **Path Parameters**:
  - `id`: Long (required)
- **Request Body**: ProductVariant object with fields to update
  ```json
  {
    "productId": 456,
    "color": "Blue",
    "size": "L",
    "stockQuantity": 80,
    "price": 11.99,
    "sku": "SKU-BLU-L",
    "isActive": true
  }
  ```
- **Response**: Updated `ProductVariant` object
- **Error Response**: `404 Not Found` if variant doesn't exist


## Cart Management (`/api/carts`)

### 28. Get All Carts
- **Endpoint**: `GET /api/carts`
- **Description**: Retrieves all shopping carts.
- **Parameters**: None
- **Response**: Array of Cart objects
  ```json
  [
    {
      "cartId": 123,
      "email": "user@example.com",
      "totalPrice": 199.98,
      "isActive": true/false,
      "createdAt": "timestamp",
      "updatedAt": "timestamp"
    }
  ]
  ```

---

### 29. Get Cart by ID
- **Endpoint**: `GET /api/carts/{id}`
- **Description**: Retrieves a specific cart by ID.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**: Cart object (includes cartProducts relation)
- **Error Response**: `404 Not Found` if cart doesn't exist

---

### 29a. Get Cart by Email ⭐ **NEW**
- **Endpoint**: `GET /api/carts/user/{email}`
- **Description**: Retrieves a user's cart by email address. Useful for frontend to fetch cart using logged-in user's email.
- **Path Parameters**:
  - `email`: string (required)
- **Response**: Cart object (includes cartProducts relation)
- **Error Response**: `404 Not Found` if cart doesn't exist for the email

---

### 30. Create Cart
- **Endpoint**: `POST /api/carts`
- **Description**: Creates a new shopping cart.
- **Request Body**: Cart object
  ```json
  {
    "email": "string (required)",
    "totalPrice": 0.00,
    "isActive": true/false
  }
  ```
- **Response**: Created Cart object

---

### 31. Update Cart
- **Endpoint**: `PUT /api/carts/{id}`
- **Description**: Updates cart information.
- **Path Parameters**:
  - `id`: Long (required)
- **Request Body**: Cart object with updated fields
  ```json
  {
    "totalPrice": 199.98,
    "isActive": true/false
  }
  ```
- **Response**: Updated Cart object
- **Error Response**: `404 Not Found` if cart doesn't exist

---

### 32. Delete Cart
- **Endpoint**: `DELETE /api/carts/{id}`
- **Description**: Deletes a cart.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**:
  ```json
  {
    "deleted": true
  }
  ```
- **Error Response**: `404 Not Found` if cart doesn't exist

---

### 32a. Sync Cart ⭐ **NEW**
- **Endpoint**: `POST /api/carts/sync`
- **Description**: Synchronizes a user's cart with the backend. Creates cart if doesn't exist, calculates total from cart items.
- **Request Body**:
  ```json
  {
    "email": "user@example.com" (required)
  }
  ```
- **Response**:
  ```json
  {
    "cart_id": 123,
    "email": "user@example.com",
    "total_price": 299.97,
    "item_count": 3,
    "synced": true
  }
  ```
- **Error Response**: `400 Bad Request` with error message

---

### 32b. Calculate Cart Total ⭐ **NEW**
- **Endpoint**: `PUT /api/carts/{id}/calculate-total`
- **Description**: Recalculates and updates cart total price based on all cart products.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**:
  ```json
  {
    "cart_id": 123,
    "total_price": 299.97,
    "item_count": 3
  }
  ```
- **Error Response**: 
  - `404 Not Found`: "Cart not found"
  - `400 Bad Request`: Error message

---

## Cart Product Management (`/api/cart-products`)

### 33. Get All Cart Products
- **Endpoint**: `GET /api/cart-products`
- **Description**: Retrieves all cart items.
- **Parameters**: None
- **Response**: Array of CartProduct objects
  ```json
  [
    {
      "id": 123,
      "variantId": 456,
      "cartId": 789,
      "orderId": null,
      "quantity": 2,
      "priceAtTime": 99.99,
      "createdAt": "timestamp"
    }
  ]
  ```

---

### 34. Get Cart Product by ID
- **Endpoint**: `GET /api/cart-products/{id}`
- **Description**: Retrieves a specific cart item by ID.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**: CartProduct object
- **Error Response**: 
  ```json
  {
    "message": "Cart product not found"
  }
  ```
  Status: `404 Not Found`

---

### 34a. Get Cart Products by Cart ID ⭐ **NEW**
- **Endpoint**: `GET /api/cart-products/cart/{cartId}`
- **Description**: Retrieves all items in a specific cart. Essential for displaying cart contents.
- **Path Parameters**:
  - `cartId`: Long (required)
- **Response**: Array of CartProduct objects
  ```json
  [
    {
      "id": 123,
      "variantId": 456,
      "cartId": 789,
      "orderId": null,
      "quantity": 2,
      "priceAtTime": 99.99,
      "createdAt": "timestamp"
    }
  ]
  ```
- **Error Response**: `400 Bad Request` with error message

---

### 35. Add Product to Cart
- **Endpoint**: `POST /api/cart-products`
- **Description**: Adds a product variant to cart. If variant already exists in cart, increases quantity.
- **Request Body**:
  ```json
  {
    "variant_id": 123 (required),
    "cart_id": 456 (required),
    "quantity": 2 (required)
  }
  ```
- **Response**:
  ```json
  {
    "message": "Product added to cart successfully",
    "cart_product_id": 789
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: "Missing required fields"
  - `404 Not Found`: "Cart not found"
  - `400 Bad Request`: "Insufficient stock"
  - `400 Bad Request`: Error message

---

### 36. Update Product Quantity in Cart
- **Endpoint**: `PUT /api/cart-products`
- **Description**: Updates the quantity of a product in the cart.
- **Request Body**:
  ```json
  {
    "cart_id": 456 (required),
    "variant_id": 123 (required),
    "quantity": 5 (required)
  }
  ```
- **Response**:
  ```json
  {
    "message": "Product quantity updated successfully"
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: "Missing required fields"
  - `400 Bad Request`: "Requested quantity exceeds available stock"
  - `400 Bad Request`: "Cart product not found" or error message

---

### 37. Remove Product from Cart
- **Endpoint**: `DELETE /api/cart-products/{id}`
- **Description**: Removes a product from the cart.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**:
  ```json
  {
    "message": "Cart product deleted successfully"
  }
  ```
- **Error Response**: `400 Bad Request` with error message

---

## Order Management (`/api/orders`)

### 38. Get All Orders
- **Endpoint**: `GET /api/orders`
- **Description**: Retrieves all orders.
- **Parameters**: None
- **Response**: Array of Order objects
  ```json
  [
    {
      "orderId": 123,
      "email": "user@example.com",
      "address": "string",
      "phoneNum": "string",
      "totalPrice": 199.98,
      "orderStatus": "pending|processing|shipped|delivered|cancelled",
      "paymentStatus": "pending|paid|failed|refunded",
      "paymentMethod": "string",
      "orderDate": "timestamp",
      "updatedAt": "timestamp"
    }
  ]
  ```

---

### 39. Get Order by ID
- **Endpoint**: `GET /api/orders/{id}`
- **Description**: Retrieves a specific order by ID.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**: Order object (includes orderItems relation)
- **Error Response**: `404 Not Found` if order doesn't exist

---

### 39a. Get Orders by Email ⭐ **NEW**
- **Endpoint**: `GET /api/orders/user/{email}`
- **Description**: Retrieves all orders for a specific user by email. Essential for order history functionality.
- **Path Parameters**:
  - `email`: string (required)
- **Response**: Array of Order objects
  ```json
  [
    {
      "orderId": 123,
      "email": "user@example.com",
      "address": "string",
      "phoneNum": "string",
      "totalPrice": 199.98,
      "orderStatus": "pending|processing|shipped|delivered|cancelled",
      "paymentStatus": "pending|paid|failed|refunded",
      "paymentMethod": "string",
      "orderDate": "timestamp",
      "updatedAt": "timestamp"
    }
  ]
  ```

---

### 40. Create Order
- **Endpoint**: `POST /api/orders`
- **Description**: Creates a new order manually.
- **Request Body**: Order object
  ```json
  {
    "email": "string (required)",
    "address": "string (required)",
    "phoneNum": "string (required)",
    "totalPrice": 199.98 (required),
    "orderStatus": "pending (default)",
    "paymentStatus": "pending (default)",
    "paymentMethod": "string (optional)"
  }
  ```
- **Response**: Created Order object

---

### 40a. Create Order from Cart ⭐ **NEW - CRITICAL**
- **Endpoint**: `POST /api/orders/from-cart/{cartId}`
- **Description**: Creates an order from a shopping cart. This is the **checkout endpoint** that:
  - Validates cart exists and has items
  - Validates stock availability for all cart items
  - Calculates total price automatically
  - Creates order with shipping details
  - Updates stock quantities for all variants
  - Links cart products to order
  - Optionally clears cart after order creation
- **Path Parameters**:
  - `cartId`: Long (required)
- **Request Body**:
  ```json
  {
    "address": "string (required)",
    "phone_num": "string (optional - uses account phone if not provided)",
    "payment_method": "string (optional)",
    "order_status": "pending (optional, default: pending)",
    "payment_status": "pending (optional, default: pending)",
    "clear_cart": true/false (optional - marks cart as inactive after order)
  }
  ```
- **Response**:
  ```json
  {
    "message": "Order created successfully",
    "order_id": 123,
    "total_price": 199.98,
    "order_status": "pending"
  }
  ```
- **Error Responses**:
  - `404 Not Found`: "Cart not found"
  - `400 Bad Request`: "Cart is empty"
  - `400 Bad Request`: 
    ```json
    {
      "error": "Insufficient stock for variant: 123",
      "variant_id": 123,
      "available_stock": 5,
      "requested_quantity": 10
    }
    ```
  - `400 Bad Request`: "Account not found" or other error messages

---

### 41. Update Order
- **Endpoint**: `PUT /api/orders/{id}`
- **Description**: Updates order information.
- **Path Parameters**:
  - `id`: Long (required)
- **Request Body**: Order object with fields to update
  ```json
  {
    "totalPrice": 199.98 (optional),
    "orderStatus": "shipped (optional)",
    "address": "string (optional)"
  }
  ```
- **Response**: Updated Order object
- **Error Response**: `404 Not Found` if order doesn't exist

---

### 42. Delete Order
- **Endpoint**: `DELETE /api/orders/{id}`
- **Description**: Deletes an order.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**:
  ```json
  {
    "deleted": true
  }
  ```
- **Error Response**: `404 Not Found` if order doesn't exist

---

## Image Management (`/api/images`) ⭐ **NEW**

### 43. Get All Images
- **Endpoint**: `GET /api/images`
- **Description**: Retrieves all images.
- **Parameters**: None
- **Response**: Array of Image objects
  ```json
  [
    {
      "imageId": 123,
      "productId": 456,
      "variantId": 789 (optional),
      "imageUrl": "https://example.com/image.jpg",
      "isPrimary": true/false,
      "createdAt": "timestamp"
    }
  ]
  ```

---

### 44. Get Image by ID
- **Endpoint**: `GET /api/images/{id}`
- **Description**: Retrieves a specific image by ID.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**: Image object
- **Error Response**: 
  ```json
  {
    "message": "Image not found"
  }
  ```
  Status: `404 Not Found`

---

### 45. Get Images by Product ID
- **Endpoint**: `GET /api/images/product/{productId}`
- **Description**: Retrieves all images for a specific product.
- **Path Parameters**:
  - `productId`: Long (required)
- **Response**: Array of Image objects

---

### 45a. Upload Single Image (NEW)
- **Endpoint**: `POST /api/images/upload`
- **Description**: Uploads a single image file (multipart/form-data). Returns an `imageUrl` and the created Image record. Useful when clients need to upload binary image files.
- **Request**: `multipart/form-data` with a `file` field and optional `productId`, `variantId`, `isPrimary` fields.
- **Response**: Created Image object with `imageUrl` (Status: `201 Created`)
- **Error Responses**: `400 Bad Request` when file missing or invalid

---

### 45b. Upload Multiple Images (NEW)
- **Endpoint**: `POST /api/images/upload-multiple`
- **Description**: Upload multiple image files in a single request. Returns created Image records and URLs.
- **Request**: `multipart/form-data` with multiple `files[]` fields and optional `productId`/`variantId`.
- **Response**: Array of created Image objects (Status: `201 Created`)
- **Error Responses**: `400 Bad Request` when files missing or invalid


### 46. Create Image
- **Endpoint**: `POST /api/images`
- **Description**: Creates/upload a new image for a product or variant.
- **Request Body**: Image object
  ```json
  {
    "productId": 456 (required),
    "variantId": 789 (optional),
    "imageUrl": "https://example.com/image.jpg" (required),
    "isPrimary": true/false (optional, default: false)
  }
  ```
- **Response**: Created Image object (Status: `201 Created`)
- **Error Responses**:
  - `400 Bad Request`: "Product ID is required"
  - `400 Bad Request`: "Image URL is required"
  - `400 Bad Request`: Error message

---

### 47. Update Image
- **Endpoint**: `PUT /api/images/{id}`
- **Description**: Updates image information. If setting `isPrimary` to true, automatically unsets other primary images for the same product.
- **Path Parameters**:
  - `id`: Long (required)
- **Request Body**: Image object with fields to update
  ```json
  {
    "imageUrl": "https://example.com/new-image.jpg" (optional),
    "isPrimary": true/false (optional),
    "variantId": 789 (optional)
  }
  ```
- **Response**: Updated Image object
- **Error Response**: 
  ```json
  {
    "message": "Image not found"
  }
  ```
  Status: `404 Not Found`

---

### 48. Delete Image
- **Endpoint**: `DELETE /api/images/{id}`
- **Description**: Deletes an image.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**:
  ```json
  {
    "message": "Image deleted successfully"
  }
  ```
- **Error Response**: 
  ```json
  {
    "message": "Image not found"
  }
  ```
  Status: `404 Not Found`

---

## Admin Coupons Management (`/api/admin/coupons`) ⭐ **NEW**

### 49. Get All Coupons
- **Endpoint**: `GET /api/admin/coupons`
- **Description**: Retrieves all coupons (active and inactive).
- **Parameters**: None
- **Response**: Array of Coupon objects
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

### 50. Get Active Coupons
- **Endpoint**: `GET /api/admin/coupons/active`
- **Description**: Retrieves only active coupons.
- **Parameters**: None
- **Response**: Array of active Coupon objects

---

### 51. Get Coupon by ID
- **Endpoint**: `GET /api/admin/coupons/{id}`
- **Description**: Retrieves a specific coupon by ID.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**: Coupon object
- **Error Response**: 
  ```json
  {
    "message": "Coupon not found"
  }
  ```
  Status: `404 Not Found`

---

### 52. Get Coupon by Code
- **Endpoint**: `GET /api/admin/coupons/code/{code}`
- **Description**: Retrieves a coupon by its code (case-insensitive).
- **Path Parameters**:
  - `code`: String (required)
- **Response**: Coupon object
- **Error Response**: 
  ```json
  {
    "message": "Coupon not found"
  }
  ```
  Status: `404 Not Found`

---

### 53. Create Coupon
- **Endpoint**: `POST /api/admin/coupons`
- **Description**: Creates a new coupon. Automatically converts coupon code to uppercase.
- **Request Body**:
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
- **Response**: Created Coupon object (Status: `201 Created`)
- **Error Responses**:
  - `400 Bad Request`: "Coupon code is required"
  - `400 Bad Request`: "Coupon code already exists"
  - `400 Bad Request`: "Discount type must be 'PERCENTAGE' or 'FIXED'"
  - `400 Bad Request`: "Valid until date must be after valid from date"

---

### 54. Update Coupon
- **Endpoint**: `PUT /api/admin/coupons/{id}`
- **Description**: Updates an existing coupon. Only provided fields will be updated.
- **Path Parameters**:
  - `id`: Long (required)
- **Request Body**: Coupon object with fields to update (all optional)
- **Response**: Updated Coupon object
- **Error Responses**:
  - `404 Not Found`: "Coupon not found"
  - `400 Bad Request`: "Coupon code already exists"
  - `400 Bad Request`: "Discount type must be 'PERCENTAGE' or 'FIXED'"

---

### 55. Delete Coupon
- **Endpoint**: `DELETE /api/admin/coupons/{id}`
- **Description**: Deletes a coupon.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**:
  ```json
  {
    "message": "Coupon deleted successfully"
  }
  ```
- **Error Response**: `404 Not Found`: "Coupon not found"

---

### 56. Validate Coupon ⭐ **IMPORTANT**
- **Endpoint**: `POST /api/admin/coupons/validate/{code}`
- **Description**: Validates a coupon code and calculates discount. Returns discount amount and final price.
- **Path Parameters**:
  - `code`: String (required)
- **Request Body**:
  ```json
  {
    "cart_total": 150.00 (required)
  }
  ```
- **Response (Valid Coupon)**:
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
- **Response (Invalid Coupon)**:
  ```json
  {
    "valid": false,
    "error": "Coupon has expired"
  }
  ```
- **Validation Checks**: Coupon exists, is active, within valid dates, usage limit not exceeded, minimum purchase met

---

## Admin Activity Logging (`/api/admin/activities`) ⭐ **NEW**

### 57. Get All Activities
- **Endpoint**: `GET /api/admin/activities`
- **Description**: Retrieves all admin activities with pagination.
- **Query Parameters**:
  - `page`: integer (optional, default: 0)
  - `size`: integer (optional, default: 20)
- **Response**: Paginated AdminActivity objects
  ```json
  {
    "content": [
      {
        "activityId": 1,
        "adminEmail": "admin@example.com",
        "actionType": "CREATE",
        "entityType": "PRODUCT",
        "entityId": 123,
        "description": "Created new product",
        "ipAddress": "192.168.1.1",
        "userAgent": "Mozilla/5.0...",
        "createdAt": "2024-01-15T10:30:00"
      }
    ],
    "totalElements": 150,
    "totalPages": 8
  }
  ```

---

### 58. Get Activity by ID
- **Endpoint**: `GET /api/admin/activities/{id}`
- **Description**: Retrieves a specific activity by ID.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**: AdminActivity object
- **Error Response**: `404 Not Found`: "Activity not found"

---

### 59. Get Activities by Admin Email
- **Endpoint**: `GET /api/admin/activities/admin/{email}`
- **Description**: Retrieves all activities for a specific admin with pagination.
- **Path Parameters**:
  - `email`: String (required)
- **Query Parameters**:
  - `page`: integer (optional, default: 0)
  - `size`: integer (optional, default: 20)
- **Response**: Paginated AdminActivity objects

---

### 60. Get Activities by Entity Type
- **Endpoint**: `GET /api/admin/activities/entity/{entityType}`
- **Description**: Retrieves all activities for a specific entity type.
- **Path Parameters**:
  - `entityType`: String (required) - Examples: PRODUCT, ORDER, COUPON, USER
- **Response**: Array of AdminActivity objects

---

### 61. Get Activities by Action Type
- **Endpoint**: `GET /api/admin/activities/action/{actionType}`
- **Description**: Retrieves all activities for a specific action type.
- **Path Parameters**:
  - `actionType`: String (required) - Examples: CREATE, UPDATE, DELETE, VIEW
- **Response**: Array of AdminActivity objects

---

### 62. Log Activity (Full Object)
- **Endpoint**: `POST /api/admin/activities`
- **Description**: Logs an admin activity. Automatically captures IP address and User-Agent.
- **Request Body**:
  ```json
  {
    "adminEmail": "admin@example.com" (required),
    "actionType": "CREATE" (required),
    "entityType": "PRODUCT" (required),
    "entityId": 123 (optional),
    "description": "Created new product" (optional)
  }
  ```
- **Response**: Created AdminActivity object (Status: `201 Created`)

---

### 63. Quick Log Activity ⭐ **RECOMMENDED**
- **Endpoint**: `POST /api/admin/activities/quick-log`
- **Description**: Simplified activity logging endpoint.
- **Request Body**:
  ```json
  {
    "admin_email": "admin@example.com" (required),
    "action_type": "UPDATE" (required),
    "entity_type": "ORDER" (required),
    "entity_id": 456 (optional),
    "description": "Updated order status" (optional)
  }
  ```
- **Response**: Created AdminActivity object (Status: `201 Created`)

---

### 64. Delete Activity
- **Endpoint**: `DELETE /api/admin/activities/{id}`
- **Description**: Deletes an activity log entry.
- **Path Parameters**:
  - `id`: Long (required)
- **Response**:
  ```json
  {
    "message": "Activity deleted successfully"
  }
  ```
- **Error Response**: `404 Not Found`: "Activity not found"

---

## Store Settings (`/api/admin/settings`) ⭐ **NEW**

These endpoints persist and load store-wide information that is currently hardcoded in the frontend (e.g. `storeName`, `storeEmail`, `storePhone`, `storeAddress`).

### 65. Get Store Settings
- **Endpoint**: `GET /api/admin/settings`
- **Auth**: `Authorization: Bearer <access_token>`
- **Required Role**: `ADMIN`
- **Description**: Returns the current store configuration used for display and contact information in the admin UI.
- **Request Body**: _None_
- **Headers**:
  - `Authorization: Bearer <access_token>`
- **Success Response** (200 OK):
  ```json
  {
    "storeName": "Victus Roman Arena",
    "storeEmail": "admin@victus.com",
    "storePhone": "+1 (555) 123-4567",
    "storeAddress": "123 Arena Street, Sports City, SC 12345",
    "updatedAt": "2025-01-31T12:34:56Z",
    "updatedBy": "admin@example.com"
  }
  ```
  - `updatedAt` and `updatedBy` are for audit display; if not available, they may be `null`.
- **Error Responses**:
  - `401 Unauthorized` – missing/invalid token
  - `403 Forbidden` – token valid but user is not an admin (enforced via Spring Security role)
  - `500 Internal Server Error` – unexpected failure

---

### 66. Update Store Settings
- **Endpoint**: `PUT /api/admin/settings`
- **Auth**: `Authorization: Bearer <access_token>`
- **Required Role**: `ADMIN`
- **Description**: Saves the settings edited in the admin UI (e.g. from an `AdminSettings` page “Save Settings” button).
- **Request Body**:
  ```json
  {
    "storeName": "Victus Roman Arena",
    "storeEmail": "admin@victus.com",
    "storePhone": "+1 (555) 123-4567",
    "storeAddress": "123 Arena Street, Sports City, SC 12345"
  }
  ```
- **Field Rules**:
  - `storeName`: string, **required**, non-blank
  - `storeEmail`: string, **required**, must be a valid email (basic `@` and `.` check)
  - `storePhone`: string, optional but recommended
  - `storeAddress`: string, optional but recommended
- **Success Response** (200 OK):
  ```json
  {
    "storeName": "Victus Roman Arena",
    "storeEmail": "admin@victus.com",
    "storePhone": "+1 (555) 123-4567",
    "storeAddress": "123 Arena Street, Sports City, SC 12345",
    "updatedAt": "2025-01-31T12:35:10Z",
    "updatedBy": "admin@example.com"
  }
  ```
- **Validation Errors** – `400 Bad Request`:
  - Example payloads:
    - `{ "error": "storeName is required" }`
    - `{ "error": "storeEmail is required" }`
    - `{ "error": "storeEmail is invalid" }`
- **Other Errors**:
  - `401 Unauthorized` – missing/invalid token
  - `403 Forbidden` – not an admin
  - `500 Internal Server Error`

---

## Admin Product Stock Summary (`/api/admin/products/stock-summary`) ⭐ **NEW**

### 67. Get Stock Summary for All Products
- **Endpoint**: `GET /api/admin/products/stock-summary`
- **Auth**: `Authorization: Bearer <access_token>`
- **Required Role**: `ADMIN`
- **Description**: Returns total stock per product and whether each product is considered “low stock”, for use in admin dashboards (e.g. the “Stock” column in `AdminProducts`).
- **Request Body**: _None_
- **Query Parameters**: _None_ (the frontend can filter client-side; optional future support for `?productIds=1,2,3`).
- **Success Response** (200 OK):
  ```json
  [
    {
      "productId": 1,
      "totalStock": 120,
      "lowStock": false
    },
    {
      "productId": 2,
      "totalStock": 5,
      "lowStock": true
    }
  ]
  ```
- **Notes**:
  - `totalStock` is computed as `SUM(stockQuantity)` over all variants per product.
  - `lowStock` is `true` when `totalStock` is **less than 10** (threshold is configurable in code).
- **Error Responses**:
  - `401 Unauthorized` – missing/invalid token
  - `403 Forbidden` – not an admin
  - `500 Internal Server Error`

---

## Important Notes

### Authentication
- JWT tokens are generated on login/register and expire after 24 hours
- Include token in `Authorization` header: `Bearer <token>` (if authentication is enabled)

### CORS
- CORS is enabled for all origins (`*`) on `/api/**` endpoints

### Database Relations
- Deleting an Account cascades to related Sellers, Carts, and Orders
- Deleting a Product cascades to ProductVariants and Images
- Deleting a Cart cascades to CartProducts
- Deleting an Order sets CartProducts' orderId to null

### Status Values
- **Order Status**: `pending`, `processing`, `shipped`, `delivered`, `cancelled`
- **Payment Status**: `pending`, `paid`, `failed`, `refunded`

### Pagination
- Products endpoint supports pagination with `page` and `size` query parameters
- Default: page=0, size=10

### New Features (Latest Updates)
- ✅ **Create Order from Cart**: Complete checkout flow with automatic stock updates
- ✅ **Get Cart by Email**: Access user's cart using email
- ✅ **Get Orders by Email**: View user's order history
- ✅ **Get Cart Products by Cart ID**: Display all items in a cart
- ✅ **Image Management**: Full CRUD operations for product/variant images
- ✅ **Cart Sync**: Synchronize cart with backend and auto-calculate totals
- ✅ **Admin Coupons**: Complete coupon management with validation
- ✅ **Admin Activity Logging**: Full audit trail for admin actions

---

## Swagger Documentation
API documentation is available at:
```
http://localhost:8080/swagger-ui.html
```
or
```
http://localhost:8080/swagger-ui/index.html
```

---

## Summary

**Total Endpoints**: 67 (previously 42)

### Endpoint Breakdown:
- Authentication: 3 endpoints
- Account Management: 5 endpoints
- Seller Management: 5 endpoints
- Category Management: 5 endpoints
- Product Management: 5 endpoints
- Product Variant Management: 6 endpoints
- Cart Management: 8 endpoints (includes sync and calculate-total)
- Cart Product Management: 6 endpoints
- Order Management: 7 endpoints (includes Get Orders by Email and Create Order from Cart)
- Image Management: 8 endpoints
- **Admin Coupons Management: 8 endpoints** ⭐ **NEW**
- **Admin Activity Logging: 8 endpoints** ⭐ **NEW**

### Key Features:
- ✅ Complete checkout flow (Cart → Order)
- ✅ Automatic stock management
- ✅ User cart and order history access
- ✅ Full image management for products
- ✅ **Cart synchronization with backend**
- ✅ **Coupon management with validation and discount calculation**
- ✅ **Complete admin activity audit trail**
- ✅ All endpoints include error handling and validation

