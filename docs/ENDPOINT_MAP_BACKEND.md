# Backend Endpoint Map - VictusStore

**Version**: 2.0.1  
**Last Updated**: Latest  
**Total Endpoints**: 78

---

## Legend

- **Auth Required**: ✅ = Yes, ❌ = No
- **Role Required**: CUSTOMER, SELLER, ADMIN, or ❌ = None
- **Rate Limited**: ✅ = Yes, ❌ = No
- **Idempotent**: ✅ = Yes (supports Idempotency-Key header)

---

## Authentication Endpoints

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| POST | `/api/auth/register` | ❌ | ❌ | ✅ (3/min) | Map<String, Object> | `{access_token, refresh_token, email, role}` | `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| POST | `/api/auth/login` | ❌ | ❌ | ✅ (5/min) | Map<String, String> | `{access_token, refresh_token, email, role}` | `INVALID_CREDENTIALS`, `RUNTIME_ERROR` |
| POST | `/api/auth/refresh` | ❌ | ❌ | ❌ | `{refresh_token: string}` | `{access_token, refresh_token, email, role}` | `INVALID_CREDENTIALS`, `RUNTIME_ERROR` |
| GET | `/api/auth/check_account/{email}/{password}` | ❌ | ❌ | ❌ | Path params | `{exists, password, is_seller, role}` | `RUNTIME_ERROR` |

---

## Health & Monitoring

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| GET | `/api/health` | ❌ | ❌ | ❌ | None | `{status, application, version, uptime, database}` | `INTERNAL_ERROR` |

---

## Account Management

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| GET | `/api/accounts` | ✅ | ❌ | ❌ | None | `List<Account>` | `ACCESS_DENIED`, `RUNTIME_ERROR` |
| GET | `/api/accounts/{email}` | ✅ | ❌ | ❌ | Path param | `Account` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/accounts` | ✅ | ❌ | ❌ | `Account` | `Account` | `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| PUT | `/api/accounts/{email}` | ✅ | ❌ | ❌ | `Account` | `Account` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| DELETE | `/api/accounts/{email}` | ✅ | ❌ | ❌ | Path param | `{deleted: boolean}` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |

---

## Seller Management

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| GET | `/api/sellers` | ✅ | ❌ | ❌ | None | `List<Seller>` | `RUNTIME_ERROR` |
| GET | `/api/sellers/{id}` | ✅ | ❌ | ❌ | Path param | `Seller` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/sellers` | ✅ | ❌ | ❌ | `Seller` | `Seller` | `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| PUT | `/api/sellers/{id}` | ✅ | ❌ | ❌ | `Seller` | `Seller` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| DELETE | `/api/sellers/{id}` | ✅ | ❌ | ❌ | Path param | `{deleted: boolean}` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |

---

## Category Management

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| GET | `/api/categories` | ✅ | ❌ | ❌ | None | `List<Category>` | `RUNTIME_ERROR` |
| GET | `/api/categories/{id}` | ✅ | ❌ | ❌ | Path param | `Category` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/categories` | ✅ | ❌ | ❌ | `Category` | `Category` | `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| PUT | `/api/categories/{id}` | ✅ | ❌ | ❌ | `Category` | `Category` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| DELETE | `/api/categories/{id}` | ✅ | ❌ | ❌ | Path param | `{deleted: boolean}` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |

---

## Product Management

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| GET | `/api/products` | ✅ | ❌ | ❌ | Query: `page, size` | `Page<Product>` | `RUNTIME_ERROR` |
| GET | `/api/products/{id}` | ✅ | ❌ | ❌ | Path param | `Product` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| GET | `/api/products/category/{categoryId}` | ✅ | ❌ | ❌ | Path param | `List<Product>` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/products` | ✅ | ❌ | ❌ | `Product` | `Product` | `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| PUT | `/api/products/{id}` | ✅ | ❌ | ❌ | `Product` | `Product` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| DELETE | `/api/products/{id}` | ✅ | ❌ | ❌ | Path param | `{deleted: boolean}` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |

---

## Product Variant Management

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| GET | `/api/variants` | ✅ | ❌ | ❌ | None | `List<ProductVariant>` | `RUNTIME_ERROR` |
| GET | `/api/variants/{id}` | ✅ | ❌ | ❌ | Path param | `ProductVariant` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| GET | `/api/variants/{id}/check-availability` | ✅ | ❌ | ❌ | Path param | `{variant_id, color, size, stock_quantity, product_name}` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| GET | `/api/variants/product/{productId}` | ✅ | ❌ | ❌ | Path param | `List<ProductVariant>` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/variants` | ✅ | ❌ | ❌ | `ProductVariant` | `ProductVariant` | `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| PUT | `/api/variants/{id}` | ✅ | ❌ | ❌ | `ProductVariant` | `ProductVariant` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |

---

## Cart Management

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| GET | `/api/carts` | ✅ | ❌ | ❌ | None | `List<Cart>` | `RUNTIME_ERROR` |
| GET | `/api/carts/{id}` | ✅ | ❌ | ❌ | Path param | `Cart` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| GET | `/api/carts/user/{email}` | ✅ | ❌ | ❌ | Path param | `Cart` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/carts` | ✅ | ❌ | ❌ | `Cart` | `Cart` | `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| POST | `/api/carts/sync` | ✅ | ❌ | ❌ | `{email: string}` | `{cart_id, email, total_price, item_count, synced}` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| PUT | `/api/carts/{id}` | ✅ | ❌ | ❌ | `Cart` | `Cart` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| PUT | `/api/carts/{id}/calculate-total` | ✅ | ❌ | ❌ | Path param | `{cart_id, total_price, item_count}` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| DELETE | `/api/carts/{id}` | ✅ | ❌ | ❌ | Path param | `{deleted: boolean}` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |

---

## Cart Product Management

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| GET | `/api/cart-products` | ✅ | ❌ | ❌ | None | `List<CartProduct>` | `RUNTIME_ERROR` |
| GET | `/api/cart-products/{id}` | ✅ | ❌ | ❌ | Path param | `CartProduct` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| GET | `/api/cart-products/cart/{cartId}` | ✅ | ❌ | ❌ | Path param | `List<CartProduct>` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/cart-products` | ✅ | ❌ | ❌ | `{variant_id, cart_id, quantity}` | `{message, cart_product_id}` | `INVALID_ARGUMENT`, `STOCK_INSUFFICIENT`, `RUNTIME_ERROR` |
| PUT | `/api/cart-products` | ✅ | ❌ | ❌ | `{cart_id, variant_id, quantity}` | `{message}` | `INVALID_ARGUMENT`, `STOCK_INSUFFICIENT`, `RUNTIME_ERROR` |
| DELETE | `/api/cart-products/{id}` | ✅ | ❌ | ❌ | Path param | `{message}` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |

---

## Order Management

| Method | Endpoint | Auth | Role | Rate Limit | Idempotent | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|------------|-------------|---------------|-------------|
| GET | `/api/orders` | ✅ | ❌ | ❌ | ❌ | None | `List<Order>` | `RUNTIME_ERROR` |
| GET | `/api/orders/with-products` | ✅ | ❌ | ❌ | ❌ | None | `List<Order>` | `RUNTIME_ERROR` |
| GET | `/api/orders/{id}` | ✅ | ❌ | ❌ | ❌ | Path param | `Order` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| GET | `/api/orders/user/{email}` | ✅ | ❌ | ❌ | ❌ | Path param | `List<Order>` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/orders` | ✅ | ❌ | ❌ | ❌ | `Order` | `Order` | `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| POST | `/api/orders/from-cart/{cartId}` | ✅ | ❌ | ✅ (10/min) | ✅ | `CreateOrderRequest` + Header: `Idempotency-Key` | `{message, order_id, total_price, order_status, order_items}` | `INVALID_ARGUMENT`, `STOCK_INSUFFICIENT`, `RUNTIME_ERROR` |
| PUT | `/api/orders/{id}` | ✅ | ❌ | ❌ | ❌ | `Order` | `Order` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| DELETE | `/api/orders/{id}` | ✅ | ❌ | ❌ | ❌ | Path param | `{deleted: boolean}` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |

**Note**: `POST /api/orders/from-cart/{cartId}` is the critical checkout endpoint with:
- Idempotency support via `Idempotency-Key` header
- Transaction boundary with `@Transactional`
- Pessimistic locking for stock safety
- Rate limiting (10 requests/minute)

---

## Image Management

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| GET | `/api/images` | ✅ | ❌ | ❌ | None | `List<Image>` | `RUNTIME_ERROR` |
| GET | `/api/images/{id}` | ✅ | ❌ | ❌ | Path param | `Image` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| GET | `/api/images/product/{productId}` | ✅ | ❌ | ❌ | Path param | `List<Image>` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/images/upload` | ✅ | ❌ | ❌ | `multipart/form-data` (file, productId, variantId, isPrimary) | `Image` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/images/upload-multiple` | ✅ | ❌ | ❌ | `multipart/form-data` (files[], productId, variantId) | `List<Image>` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/images` | ✅ | ❌ | ❌ | `Image` | `Image` | `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| PUT | `/api/images/{id}` | ✅ | ❌ | ❌ | `Image` | `Image` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| DELETE | `/api/images/{id}` | ✅ | ❌ | ❌ | Path param | `{message}` | `INVALID_ARGUMENT`, `RUNTIME_ERROR` |

---

## Admin Coupon Management

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| GET | `/api/admin/coupons` | ✅ | ADMIN | ❌ | None | `List<Coupon>` | `ACCESS_DENIED`, `RUNTIME_ERROR` |
| GET | `/api/admin/coupons/active` | ✅ | ADMIN | ❌ | None | `List<Coupon>` | `ACCESS_DENIED`, `RUNTIME_ERROR` |
| GET | `/api/admin/coupons/{id}` | ✅ | ADMIN | ❌ | Path param | `Coupon` | `ACCESS_DENIED`, `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| GET | `/api/admin/coupons/code/{code}` | ✅ | ADMIN | ❌ | Path param | `Coupon` | `ACCESS_DENIED`, `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/admin/coupons` | ✅ | ADMIN | ❌ | `Coupon` | `Coupon` | `ACCESS_DENIED`, `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| PUT | `/api/admin/coupons/{id}` | ✅ | ADMIN | ❌ | `Coupon` | `Coupon` | `ACCESS_DENIED`, `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| DELETE | `/api/admin/coupons/{id}` | ✅ | ADMIN | ❌ | Path param | `{message}` | `ACCESS_DENIED`, `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/admin/coupons/validate/{code}` | ✅ | ADMIN | ✅ (20/min) | `{cart_total: number}` | `{valid, coupon_code, discount, final_amount}` or `{valid: false, error}` | `ACCESS_DENIED`, `INVALID_ARGUMENT`, `RUNTIME_ERROR` |

---

## Admin Activity Logging

| Method | Endpoint | Auth | Role | Rate Limit | Request DTO | Response Type | Error Codes |
|--------|----------|------|------|------------|-------------|---------------|-------------|
| GET | `/api/admin/activities` | ✅ | ADMIN | ❌ | Query: `page, size` | `Page<AdminActivity>` | `ACCESS_DENIED`, `RUNTIME_ERROR` |
| GET | `/api/admin/activities/{id}` | ✅ | ADMIN | ❌ | Path param | `AdminActivity` | `ACCESS_DENIED`, `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| GET | `/api/admin/activities/admin/{email}` | ✅ | ADMIN | ❌ | Path param + Query: `page, size` | `Page<AdminActivity>` | `ACCESS_DENIED`, `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| GET | `/api/admin/activities/entity/{entityType}` | ✅ | ADMIN | ❌ | Path param | `List<AdminActivity>` | `ACCESS_DENIED`, `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| GET | `/api/admin/activities/action/{actionType}` | ✅ | ADMIN | ❌ | Path param | `List<AdminActivity>` | `ACCESS_DENIED`, `INVALID_ARGUMENT`, `RUNTIME_ERROR` |
| POST | `/api/admin/activities` | ✅ | ADMIN | ❌ | `AdminActivity` | `AdminActivity` | `ACCESS_DENIED`, `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| POST | `/api/admin/activities/quick-log` | ✅ | ADMIN | ❌ | `{admin_email, action_type, entity_type, entity_id, description}` | `AdminActivity` | `ACCESS_DENIED`, `VALIDATION_ERROR`, `RUNTIME_ERROR` |
| DELETE | `/api/admin/activities/{id}` | ✅ | ADMIN | ❌ | Path param | `{message}` | `ACCESS_DENIED`, `INVALID_ARGUMENT`, `RUNTIME_ERROR` |

---

## Error Code Reference

| Code | HTTP Status | Description | Common Causes |
|------|-------------|-------------|---------------|
| `INVALID_ARGUMENT` | 400 | Invalid input parameter | Missing required field, invalid format |
| `VALIDATION_ERROR` | 400 | Jakarta validation failed | Constraint violations, size limits |
| `CONSTRAINT_VIOLATION` | 400 | Database constraint violation | Unique constraint, foreign key |
| `STOCK_INSUFFICIENT` | 400 | Not enough stock available | Requested quantity > available stock |
| `ACCESS_DENIED` | 403 | Insufficient permissions | Non-admin accessing admin endpoint |
| `INVALID_CREDENTIALS` | 401 | Authentication failed | Wrong email/password, expired token |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests | Rate limit exceeded |
| `RUNTIME_ERROR` | 500 | General runtime error | Unexpected exception |
| `INTERNAL_ERROR` | 500 | Unexpected server error | Unhandled exception |

---

## Request/Response Examples

### Checkout (Idempotent)
```http
POST /api/orders/from-cart/123
Idempotency-Key: unique-key-here
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "address": "123 Main St",
  "phone_num": "1234567890",
  "payment_method": "credit_card",
  "clear_cart": true
}
```

**Response**:
```json
{
  "message": "Order created successfully",
  "order_id": 456,
  "total_price": 199.98,
  "order_status": "pending",
  "order_items": [...]
}
```

### Refresh Token
```http
POST /api/auth/refresh
Content-Type: application/json

{
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response**:
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com",
  "role": "CUSTOMER"
}
```

### Standard Error Response
```json
{
  "error": {
    "code": "STOCK_INSUFFICIENT",
    "message": "Not enough stock for variant 123. Available: 5, Requested: 10",
    "details": {
      "variant_id": 123,
      "available_stock": 5,
      "requested_quantity": 10
    },
    "traceId": "550e8400-e29b-41d4-a716-446655440000"
  }
}
```

---

## Authentication Header Format

All authenticated endpoints require:
```http
Authorization: Bearer <access_token>
```

Access tokens expire after 15 minutes. Use refresh token endpoint to obtain new tokens.

---

## Rate Limiting Details

| Endpoint Pattern | Limit | Window | Key |
|------------------|-------|--------|-----|
| `/api/auth/login` | 5 | 60s | IP + ":login" |
| `/api/auth/register` | 3 | 60s | IP + ":register" |
| `/api/orders/from-cart` | 10 | 60s | IP + ":checkout" |
| `/api/admin/coupons/validate` | 20 | 60s | IP + ":validate" |

Rate limit exceeded returns: `429 Too Many Requests` with `RATE_LIMIT_EXCEEDED` error code.

---

## Idempotency Details

Only `POST /api/orders/from-cart/{cartId}` supports idempotency.

**Header**: `Idempotency-Key: <unique-key>`

**Behavior**:
- First request: Processes order, stores response
- Subsequent requests with same key: Returns cached response (same order_id)
- Key scope: Per user + endpoint (composite unique constraint)
- TTL: 24 hours (configurable via `app.idempotency.ttl-hours`)

---

## Notes

1. **All endpoints** return standard error format with `traceId` in error responses
2. **Admin endpoints** require `ROLE_ADMIN` (checked via Spring Security)
3. **Rate limiting** is IP-based (in-memory). For distributed systems, use Redis.
4. **Validation** is applied where DTOs are used. Most endpoints use entities directly.
5. **CORS** is configured globally. Origins configurable via `app.cors.allowed-origins`

---

**Last Updated**: Latest  
**Total Endpoints**: 78  
**Public Endpoints**: 5 (auth + health)  
**Authenticated Endpoints**: 64  
**Admin-Only Endpoints**: 17

