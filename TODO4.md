# TODO4: Investigate Order Product Mismatch Bug

## Issue Description
User reports ordering a "ball" but the order records show "Tennis Racket" (variantId 2, productId 2).

## Information Gathered
- Order creation logic in `OrderController.createOrderFromCart` copies `CartProduct` items from cart to order.
- `CartProduct` contains `variantId` which links to `ProductVariant`, then to `Product`.
- In the provided JSON, variantId 2 corresponds to "Tennis Racket" product.
- No "ball" product found in codebase or database scripts.
- Cart addition in `CartProductController.createCartProduct` accepts `variant_id` from request without additional validation beyond stock check.

## Potential Causes
1. **Frontend Error**: Wrong `variant_id` sent when adding to cart.
2. **User Error**: Selected wrong product variant.
3. **Data Issue**: Products/variants misconfigured in database.
4. **Validation Gap**: No validation that `variant_id` corresponds to expected product.

## Plan
1. **Add Validation in Cart Addition**:
   - Validate that `variant_id` exists and is active.
   - Optionally, accept `product_id` in request and validate variant belongs to product.

2. **Enhance Order/Cart Response**:
   - Include product name and variant details in cart/order JSON responses for better debugging.

3. **Add Logging**:
   - Log cart additions and order creations with product details.

4. **Database Check**:
   - Ensure sample data has correct products (e.g., Tennis Ball if intended).

## Dependent Files to Edit
- `CartProductController.java`: Add validation for variant existence and activity.
- `OrderController.java`: Enhance response with product details.
- `CartProduct.java`: Ensure variant relationship is properly loaded.

## Followup Steps
- Test cart addition with invalid variant_id.
- Verify order creation includes correct product info.
- Check database for product catalog.
