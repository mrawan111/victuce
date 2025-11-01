# VictusStore API - Complete Endpoints Documentation

## Base URL
```
http://localhost:8080/api
```

---

## Authentication Endpoints (`/api/auth`)

### 1. Register User
- **Endpoint**: `POST /api/auth/register`
- **Description**: Creates a new user account. If seller_account is true, also creates a seller profile.
- **Request Body**:
  ```json
  {
    "email": "string (required)",
    "password": "string (required, min 8 chars)",
    "first_name": "string (optional)",
    "last_name": "string (optional)",
    "phone_num": "string (optional)",
    "seller_account": "boolean (optional, default: false)"
  }
  ```
- **Response**: 
  ```json
  {
    "message": "Account created successfully",
    "email": "user@example.com",
    "token": "jwt_token_here",
    "seller_id": 123 (if seller_account is true)
  }
  ```
- **Error Responses**: 
  - `400 Bad Request`: "Email already exists"
  - `400 Bad Request`: Error message on failure

---

### 2. Login User
- **Endpoint**: `POST /api/auth/login`
- **Description**: Authenticates user and returns JWT token.
- **Request Body**:
  ```json
  {
    "email": "string (required)",
    "password": "string (required)"
  }
  ```
- **Response**:
  ```json
  {
    "message": "Login successful",
    "token": "jwt_token_here",
    "email": "user@example.com",
    "seller_account": true/false
  }
  ```
- **Error Responses**:
  - `400 Bad Request`: "Invalid credentials"

---

### 3. Check Account
- **Endpoint**: `GET /api/auth/check_account/{email}/{password}`
- **Description**: Verifies if account exists and password matches.
- **Path Parameters**:
  - `email`: string (required)
  - `password`: string (required)
- **Response (Valid)**:
  ```json
  {
    "password": true,
    "exists": true,
    "is_seller": true/false
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
    "password": "string (will be hashed)",
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

**Total Endpoints**: 64 (previously 42)

### Endpoint Breakdown:
- Authentication: 3 endpoints
- Account Management: 5 endpoints
- Seller Management: 5 endpoints
- Category Management: 5 endpoints
- Product Management: 5 endpoints
- Product Variant Management: 4 endpoints
- Cart Management: 8 endpoints (includes sync and calculate-total)
- Cart Product Management: 6 endpoints
- Order Management: 7 endpoints (includes Get Orders by Email and Create Order from Cart)
- Image Management: 6 endpoints
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

