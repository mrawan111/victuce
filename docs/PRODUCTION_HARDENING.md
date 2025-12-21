# Production Hardening Implementation

**Date**: Latest  
**Version**: 2.0.0  
**Status**: Complete

---

## Overview

This document describes all production-ready hardening features implemented for the VictusStore backend. These changes ensure the system is secure, reliable, and production-ready.

---

## 1. Idempotent Checkout ✅

### Implementation
- **Table**: `idempotency_keys` (UUID primary key)
- **Service**: `IdempotencyService`
- **Endpoint**: `POST /api/orders/from-cart/{cartId}`

### Features
- Supports `Idempotency-Key` header
- Returns cached response for duplicate requests
- Request hash validation
- Configurable TTL (default: 24 hours)
- Automatic expiration cleanup

### Usage
```http
POST /api/orders/from-cart/123
Idempotency-Key: unique-key-here
Content-Type: application/json

{
  "address": "123 Main St",
  "phone_num": "1234567890",
  "clear_cart": true
}
```

### Database Migration
Run: `database/migration_add_production_features.sql`

---

## 2. Stock Safety & Transactions ✅

### Implementation
- **Transaction Boundary**: `@Transactional` on checkout endpoint
- **Pessimistic Locking**: `PESSIMISTIC_WRITE` on variant queries
- **Stock Validation**: Prevents stock from going below zero
- **Exception**: `StockInsufficientException` with details

### Features
- Row-level locking prevents race conditions
- Atomic stock updates
- Transaction rollback on failure
- Detailed error messages with variant ID, available stock, requested quantity

### Error Response
```json
{
  "error": {
    "code": "STOCK_INSUFFICIENT",
    "message": "Not enough stock for variant 123",
    "details": {
      "variant_id": 123,
      "available_stock": 5,
      "requested_quantity": 10
    },
    "traceId": "uuid-here"
  }
}
```

---

## 3. Auth Hardening ✅

### Refresh Token Support
- **Table**: `refresh_tokens` (UUID primary key)
- **Endpoint**: `POST /api/auth/refresh`
- **Service**: `RefreshTokenService`
- **Token Rotation**: Old tokens revoked when new ones issued

### Access Token Changes
- **Expiration**: 15 minutes (was 24 hours)
- **Claims**: Includes `role` (CUSTOMER, SELLER, ADMIN)
- **Backward Compatible**: Still supports old token format

### Refresh Token
- **Expiration**: 7 days (configurable)
- **Storage**: Database with revocation support
- **Rotation**: Automatic on refresh

### Role-Based Access Control
- **Roles**: CUSTOMER, SELLER, ADMIN
- **Admin Protection**: All `/api/admin/**` endpoints require ADMIN role
- **JWT Filter**: `JwtAuthenticationFilter` extracts role from token

### Updated Endpoints
- `POST /api/auth/register` - Returns `access_token` and `refresh_token`
- `POST /api/auth/login` - Returns `access_token` and `refresh_token`
- `POST /api/auth/refresh` - New endpoint for token refresh

### Response Format
```json
{
  "access_token": "jwt-token-here",
  "refresh_token": "refresh-token-here",
  "email": "user@example.com",
  "role": "CUSTOMER"
}
```

---

## 4. Standard Error Contract ✅

### Implementation
- **Global Exception Handler**: `GlobalExceptionHandler`
- **Error Response Format**: Standardized JSON structure
- **Trace ID**: Included in all error responses

### Error Response Structure
```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {},
    "traceId": "uuid-here"
  }
}
```

### Supported Exceptions
- `IllegalArgumentException` → `INVALID_ARGUMENT`
- `RuntimeException` → `RUNTIME_ERROR`
- `MethodArgumentNotValidException` → `VALIDATION_ERROR`
- `ConstraintViolationException` → `CONSTRAINT_VIOLATION`
- `AccessDeniedException` → `ACCESS_DENIED`
- `BadCredentialsException` → `INVALID_CREDENTIALS`
- `StockInsufficientException` → `STOCK_INSUFFICIENT`
- Generic `Exception` → `INTERNAL_ERROR`

### Trace ID Filter
- **Filter**: `TraceIdFilter`
- **Header**: `X-Trace-Id` (auto-generated if not provided)
- **MDC**: Stored in logging context for correlation

---

## 5. Observability ✅

### Structured Logging
- **MDC Integration**: Trace ID in all logs
- **Log Pattern**: Includes trace ID, timestamp, logger, level, message
- **Configuration**: `application.properties`

### Health Endpoint
- **Endpoint**: `GET /api/health`
- **Public**: No authentication required
- **Response**:
  ```json
  {
    "status": "UP",
    "application": "victus-store",
    "version": "2.0.0",
    "uptime": "2h 30m 15s",
    "uptime_seconds": 9015,
    "database": {
      "status": "UP",
      "connected": true
    }
  }
  ```

### Features
- Application status
- Uptime calculation
- Database connectivity check
- Version information

---

## 6. Security & Rate Limiting ✅

### Rate Limiting
- **Implementation**: In-memory rate limiter (can be upgraded to Redis)
- **Filter**: `RateLimitingFilter`
- **Protected Endpoints**:
  - `/api/auth/login` - 5 requests/minute
  - `/api/auth/register` - 3 requests/minute
  - `/api/orders/from-cart` - 10 requests/minute
  - `/api/admin/coupons/validate` - 20 requests/minute

### Rate Limit Response
```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests. Please try again later."
  }
}
```

### Input Validation
- **DTOs**: `CreateOrderRequest`, `LoginRequest`, `RegisterRequest`
- **Validation**: Jakarta Validation annotations
- **Sanitization**: `InputSanitizer` utility
  - SQL injection prevention
  - XSS prevention
  - Email normalization
  - Phone number cleaning

### Security Configuration
- **JWT Filter**: Automatic token validation
- **Admin Protection**: Role-based access control
- **Session Management**: Stateless (JWT only)
- **CSRF**: Disabled (using JWT)

---

## Database Changes

### New Tables
1. **idempotency_keys**
   - Stores idempotency keys and responses
   - Indexed on `key` and `user_email`
   - Auto-expires after TTL

2. **refresh_tokens**
   - Stores refresh tokens
   - Indexed on `token` and `user_email`
   - Supports revocation

### Modified Tables
1. **accounts**
   - Added `role` column (CUSTOMER, SELLER, ADMIN)
   - Default: CUSTOMER
   - Existing sellers automatically set to SELLER

### Migration Script
Run: `database/migration_add_production_features.sql`

---

## Configuration

### Application Properties
```properties
# Production Features
app.idempotency.ttl-hours=24
app.refresh-token.expiration-days=7
app.version=2.0.0

# Logging
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - [%X{traceId}] - %level - %msg%n
```

---

## API Changes Summary

### New Endpoints
- `POST /api/auth/refresh` - Refresh access token
- `GET /api/health` - Health check

### Modified Endpoints
- `POST /api/auth/register` - Returns access_token + refresh_token
- `POST /api/auth/login` - Returns access_token + refresh_token
- `POST /api/orders/from-cart/{cartId}` - Idempotent, transactional, stock-safe

### Protected Endpoints
- All `/api/admin/**` endpoints now require ADMIN role

---

## Testing Checklist

### Idempotency
- [ ] Same idempotency key returns same response
- [ ] Different idempotency keys create different orders
- [ ] Expired keys are cleaned up

### Stock Safety
- [ ] Concurrent requests don't oversell
- [ ] Stock never goes below zero
- [ ] Transaction rollback on failure

### Authentication
- [ ] Access tokens expire after 15 minutes
- [ ] Refresh tokens work correctly
- [ ] Token rotation prevents reuse
- [ ] Admin endpoints require ADMIN role

### Error Handling
- [ ] All errors follow standard format
- [ ] Trace IDs are included
- [ ] Error codes are consistent

### Rate Limiting
- [ ] Login rate limit enforced
- [ ] Register rate limit enforced
- [ ] Checkout rate limit enforced
- [ ] Coupon validation rate limit enforced

### Health Check
- [ ] Health endpoint returns correct status
- [ ] Database connectivity checked
- [ ] Uptime calculated correctly

---

## Migration Guide

### 1. Run Database Migration
```bash
psql -U postgres -d your_database -f database/migration_add_production_features.sql
```

### 2. Update Application Properties
Add the new configuration properties (see Configuration section above).

### 3. Update Frontend
- Use `access_token` instead of `token`
- Implement refresh token logic
- Include `Idempotency-Key` header for checkout
- Handle new error response format
- Include `X-Trace-Id` in requests (optional)

### 4. Test
- Run all tests
- Verify idempotency works
- Test concurrent checkout
- Verify admin endpoint protection

---

## Performance Considerations

### Idempotency
- Keys are indexed for fast lookup
- Expired keys should be cleaned periodically (consider scheduled job)

### Rate Limiting
- Current implementation is in-memory
- For distributed systems, consider Redis-based rate limiting

### Pessimistic Locking
- May cause contention under high load
- Consider optimistic locking for read-heavy scenarios

---

## Security Notes

### JWT Secret
- **Current**: Hardcoded in `JwtUtil`
- **Recommendation**: Move to environment variable or secrets manager

### Rate Limiting
- **Current**: IP-based
- **Recommendation**: Consider user-based rate limiting for authenticated users

### Input Sanitization
- Basic patterns implemented
- **Recommendation**: Consider using OWASP ESAPI for comprehensive sanitization

---

## Future Enhancements

1. **Redis-based Rate Limiting**: For distributed systems
2. **Distributed Tracing**: Integrate with OpenTelemetry
3. **Metrics**: Add Prometheus metrics
4. **Audit Logging**: Enhanced audit trail
5. **API Versioning**: Support multiple API versions
6. **Circuit Breaker**: For external service calls

---

## Summary

All production hardening features have been successfully implemented:

✅ Idempotent checkout with database storage  
✅ Stock safety with transactions and pessimistic locking  
✅ Refresh token support with rotation  
✅ Role-based access control (CUSTOMER, SELLER, ADMIN)  
✅ Standard error contract with trace IDs  
✅ Structured logging with MDC  
✅ Health check endpoint  
✅ Rate limiting for critical endpoints  
✅ Input validation and sanitization  

The backend is now production-ready and matches the hardened frontend requirements.

---

**Last Updated**: Latest  
**Version**: 2.0.0

