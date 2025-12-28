#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Comprehensive test script for VictusStore Backend API
Tests all 67+ endpoints systematically

Usage:
    python test_all_endpoints.py [--base-url BASE_URL] [--verbose]

Requirements:
    pip install requests
"""

import requests
import json
import sys
import argparse
import os
from datetime import datetime, timedelta
from typing import Dict, Any, Optional
from pathlib import Path

# Fix Windows console encoding for emojis
if sys.platform == 'win32':
    try:
        import codecs
        sys.stdout = codecs.getwriter('utf-8')(sys.stdout.buffer, 'strict')
        sys.stderr = codecs.getwriter('utf-8')(sys.stderr.buffer, 'strict')
    except:
        pass

# Color codes for terminal output
class Colors:
    GREEN = '\033[92m'
    RED = '\033[91m'
    YELLOW = '\033[93m'
    BLUE = '\033[94m'
    RESET = '\033[0m'
    BOLD = '\033[1m'

class APITester:
    def __init__(self, base_url: str = "http://localhost:8080/api", verbose: bool = False):
        self.base_url = base_url.rstrip('/')
        self.verbose = verbose
        self.session = requests.Session()
        self.results = {
            'passed': 0,
            'failed': 0,
            'skipped': 0,
            'errors': []
        }
        # Store created IDs for cleanup/testing
        self.created_ids = {
            'account_email': None,
            'seller_id': None,
            'category_id': None,
            'product_id': None,
            'variant_id': None,
            'cart_id': None,
            'cart_product_id': None,
            'order_id': None,
            'image_id': None,
            'coupon_id': None,
            'activity_id': None,
            'token': None
        }

    def log(self, message: str, color: str = Colors.RESET):
        """Print colored log message"""
        if self.verbose or color != Colors.RESET:
            try:
                print(f"{color}{message}{Colors.RESET}")
            except UnicodeEncodeError:
                # Fallback for Windows console encoding issues
                safe_message = message.encode('ascii', 'replace').decode('ascii')
                print(f"{color}{safe_message}{Colors.RESET}")

    def test_endpoint(self, method: str, endpoint: str, expected_status: int = 200,
                     data: Optional[Dict] = None, headers: Optional[Dict] = None,
                     description: str = "", skip: bool = False) -> Optional[Dict]:
        """Test a single endpoint"""
        if skip:
            self.results['skipped'] += 1
            self.log(f"⏭️  SKIP: {description or endpoint}", Colors.YELLOW)
            return None

        url = f"{self.base_url}{endpoint}"
        method_func = getattr(self.session, method.lower())
        
        try:
            self.log(f"\n{'='*60}", Colors.BLUE)
            self.log(f"Testing: {method} {endpoint}", Colors.BOLD)
            if description:
                self.log(f"Description: {description}", Colors.BLUE)
            
            kwargs = {}
            if data:
                kwargs['json'] = data
                if self.verbose:
                    self.log(f"Request Body: {json.dumps(data, indent=2)}", Colors.BLUE)
            if headers:
                kwargs['headers'] = headers

            response = method_func(url, **kwargs, timeout=10)
            
            status_ok = response.status_code == expected_status
            status_color = Colors.GREEN if status_ok else Colors.RED
            
            self.log(f"Status: {response.status_code} (expected: {expected_status})", status_color)
            
            if self.verbose:
                try:
                    response_json = response.json()
                    self.log(f"Response: {json.dumps(response_json, indent=2)}", Colors.BLUE)
                except:
                    self.log(f"Response: {response.text[:200]}", Colors.BLUE)
            
            if status_ok:
                self.results['passed'] += 1
                self.log(f"✅ PASS: {description or endpoint}", Colors.GREEN)
                try:
                    return response.json()
                except:
                    return {'text': response.text}
            else:
                self.results['failed'] += 1
                error_msg = f"❌ FAIL: {description or endpoint} - Status {response.status_code}"
                self.log(error_msg, Colors.RED)
                self.results['errors'].append({
                    'endpoint': endpoint,
                    'method': method,
                    'status': response.status_code,
                    'expected': expected_status,
                    'response': response.text[:200]
                })
                return None
                
        except requests.exceptions.RequestException as e:
            self.results['failed'] += 1
            error_msg = f"❌ ERROR: {description or endpoint} - {str(e)}"
            self.log(error_msg, Colors.RED)
            self.results['errors'].append({
                'endpoint': endpoint,
                'method': method,
                'error': str(e)
            })
            return None

    # ==================== AUTHENTICATION ENDPOINTS ====================
    def test_auth_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING AUTHENTICATION ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Test 1: Register User
        register_data = {
            "email": f"testuser_{datetime.now().timestamp()}@test.com",
            "password": "TestPassword123!",
            "first_name": "Test",
            "last_name": "User",
            "phone_num": "1234567890",
            "seller_account": False
        }
        self.created_ids['account_email'] = register_data['email']
        result = self.test_endpoint("POST", "/auth/register", 200, register_data,
                                   description="Register new user")
        if result and 'token' in result:
            self.created_ids['token'] = result['token']

        # Test 2: Login User
        login_data = {
            "email": self.created_ids['account_email'],
            "password": "TestPassword123!"
        }
        result = self.test_endpoint("POST", "/auth/login", 200, login_data,
                                   description="Login user")
        if result and 'token' in result:
            self.created_ids['token'] = result['token']

        # Test 3: Check Account
        email_encoded = self.created_ids['account_email'].replace('@', '%40')
        self.test_endpoint("GET", f"/auth/check_account/{email_encoded}/TestPassword123%21", 200,
                          description="Check account exists")

    # ==================== ACCOUNT MANAGEMENT ====================
    def test_account_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING ACCOUNT MANAGEMENT ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Get All Accounts
        self.test_endpoint("GET", "/accounts", 200, description="Get all accounts")

        # Get Account by Email
        if self.created_ids['account_email']:
            email_encoded = self.created_ids['account_email'].replace('@', '%40')
            self.test_endpoint("GET", f"/accounts/{email_encoded}", 200,
                              description="Get account by email")

        # Create Account (already done via register, but test direct endpoint)
        create_data = {
            "email": f"direct_{datetime.now().timestamp()}@test.com",
            "password": "DirectTest123!",
            "firstName": "Direct",
            "lastName": "Test"
        }
        self.test_endpoint("POST", "/accounts", 200, create_data,
                         description="Create account directly")

        # Update Account
        if self.created_ids['account_email']:
            email_encoded = self.created_ids['account_email'].replace('@', '%40')
            update_data = {
                "firstName": "Updated",
                "lastName": "Name"
            }
            self.test_endpoint("PUT", f"/accounts/{email_encoded}", 200, update_data,
                             description="Update account")

    # ==================== SELLER MANAGEMENT ====================
    def test_seller_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING SELLER MANAGEMENT ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Get All Sellers
        result = self.test_endpoint("GET", "/sellers", 200, description="Get all sellers")

        # Create Seller
        seller_data = {
            "sellerName": f"Test Seller {datetime.now().timestamp()}",
            "email": self.created_ids['account_email'] or "seller@test.com",
            "rating": 4.5,
            "isActive": True
        }
        result = self.test_endpoint("POST", "/sellers", 200, seller_data,
                                   description="Create seller")
        if result and 'sellerId' in result:
            self.created_ids['seller_id'] = result['sellerId']

        # Get Seller by ID
        if self.created_ids['seller_id']:
            seller_result = self.test_endpoint("GET", f"/sellers/{self.created_ids['seller_id']}", 200,
                             description="Get seller by ID")

        # Update Seller
        if self.created_ids['seller_id']:
            # Include all required fields to avoid null constraint violations
            # Get current seller data or use defaults
            update_data = {
                "sellerName": seller_result.get('sellerName', 'Updated Seller') if seller_result else 'Updated Seller',
                "rating": 5.0,
                "isActive": True
            }
            self.test_endpoint("PUT", f"/sellers/{self.created_ids['seller_id']}", 200, update_data,
                             description="Update seller")

    # ==================== CATEGORY MANAGEMENT ====================
    def test_category_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING CATEGORY MANAGEMENT ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Get All Categories
        self.test_endpoint("GET", "/categories", 200, description="Get all categories")

        # Create Category
        category_data = {
            "categoryName": f"Test Category {datetime.now().timestamp()}",
            "categoryImage": "https://example.com/image.jpg",
            "isActive": True
        }
        result = self.test_endpoint("POST", "/categories", 200, category_data,
                                   description="Create category")
        if result and 'categoryId' in result:
            self.created_ids['category_id'] = result['categoryId']

        # Get Category by ID
        if self.created_ids['category_id']:
            self.test_endpoint("GET", f"/categories/{self.created_ids['category_id']}", 200,
                             description="Get category by ID")

        # Update Category
        if self.created_ids['category_id']:
            # Get current category data first to preserve existing values
            category_result = self.test_endpoint("GET", f"/categories/{self.created_ids['category_id']}", 200,
                             description="Get category by ID (for update)")
            # Include all fields to avoid null constraint violations
            update_data = {
                "categoryName": "Updated Category Name",
                "categoryImage": category_result.get('categoryImage', '') if category_result else '',
                "isActive": category_result.get('isActive', True) if category_result else True
            }
            self.test_endpoint("PUT", f"/categories/{self.created_ids['category_id']}", 200, update_data,
                             description="Update category")

    # ==================== PRODUCT MANAGEMENT ====================
    def test_product_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING PRODUCT MANAGEMENT ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Get All Products (Paginated)
        self.test_endpoint("GET", "/products?page=0&size=10", 200,
                          description="Get all products (paginated)")

        # Create Product
        product_data = {
            "productName": f"Test Product {datetime.now().timestamp()}",
            "description": "Test product description",
            "basePrice": 99.99,
            "categoryId": self.created_ids.get('category_id'),
            "sellerId": self.created_ids.get('seller_id'),
            "productRating": 0.0,
            "isActive": True
        }
        result = self.test_endpoint("POST", "/products", 200, product_data,
                                   description="Create product")
        if result and 'productId' in result:
            self.created_ids['product_id'] = result['productId']

        # Get Product by ID
        if self.created_ids['product_id']:
            self.test_endpoint("GET", f"/products/{self.created_ids['product_id']}", 200,
                             description="Get product by ID")

        # Update Product
        if self.created_ids['product_id']:
            update_data = {"basePrice": 129.99}
            self.test_endpoint("PUT", f"/products/{self.created_ids['product_id']}", 200, update_data,
                             description="Update product")

    # ==================== VARIANT MANAGEMENT ====================
    def test_variant_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING VARIANT MANAGEMENT ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Get All Variants
        self.test_endpoint("GET", "/variants", 200, description="Get all variants")

        # Create Variant
        if self.created_ids['product_id']:
            variant_data = {
                "productId": self.created_ids['product_id'],
                "color": "Red",
                "size": "M",
                "stockQuantity": 100,
                "price": 9.99,
                "sku": f"SKU-{datetime.now().timestamp()}",
                "isActive": True
            }
            result = self.test_endpoint("POST", "/variants", 201, variant_data,
                                       description="Create variant")
            if result and 'variantId' in result:
                self.created_ids['variant_id'] = result['variantId']

        # Get Variant by ID
        if self.created_ids['variant_id']:
            self.test_endpoint("GET", f"/variants/{self.created_ids['variant_id']}", 200,
                             description="Get variant by ID")

        # Check Variant Availability
        if self.created_ids['variant_id']:
            self.test_endpoint("GET", f"/variants/{self.created_ids['variant_id']}/check-availability", 200,
                             description="Check variant availability")

        # Get Variants by Product ID
        if self.created_ids['product_id']:
            self.test_endpoint("GET", f"/variants/product/{self.created_ids['product_id']}", 200,
                             description="Get variants by product ID")

        # Update Variant
        if self.created_ids['variant_id']:
            # Get current variant data first to preserve required fields (color, size are required)
            variant_result = self.test_endpoint("GET", f"/variants/{self.created_ids['variant_id']}", 200,
                             description="Get variant by ID (for update)")
            # Include all required fields to avoid null constraint violations
            update_data = {
                "productId": variant_result.get('productId') if variant_result else self.created_ids['product_id'],
                "color": variant_result.get('color', 'Red') if variant_result else 'Red',
                "size": variant_result.get('size', 'M') if variant_result else 'M',
                "stockQuantity": 150,
                "price": variant_result.get('price', 9.99) if variant_result else 9.99,
                "sku": variant_result.get('sku', '') if variant_result else '',
                "isActive": variant_result.get('isActive', True) if variant_result else True
            }
            self.test_endpoint("PUT", f"/variants/{self.created_ids['variant_id']}", 200, update_data,
                             description="Update variant")

    # ==================== CART MANAGEMENT ====================
    def test_cart_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING CART MANAGEMENT ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Get All Carts
        self.test_endpoint("GET", "/carts", 200, description="Get all carts")

        # Create Cart
        cart_data = {
            "email": self.created_ids['account_email'] or "test@test.com",
            "totalPrice": 0.00,
            "isActive": True
        }
        result = self.test_endpoint("POST", "/carts", 200, cart_data,
                                   description="Create cart")
        if result and 'cartId' in result:
            self.created_ids['cart_id'] = result['cartId']

        # Get Cart by ID
        if self.created_ids['cart_id']:
            self.test_endpoint("GET", f"/carts/{self.created_ids['cart_id']}", 200,
                             description="Get cart by ID")

        # Get Cart by Email
        if self.created_ids['account_email']:
            email_encoded = self.created_ids['account_email'].replace('@', '%40')
            self.test_endpoint("GET", f"/carts/user/{email_encoded}", 200,
                             description="Get cart by email")

        # Sync Cart
        if self.created_ids['account_email']:
            sync_data = {"email": self.created_ids['account_email']}
            result = self.test_endpoint("POST", "/carts/sync", 200, sync_data,
                                       description="Sync cart")
            if result and 'cart_id' in result:
                self.created_ids['cart_id'] = result['cart_id']

        # Update Cart
        if self.created_ids['cart_id']:
            update_data = {"totalPrice": 99.99}
            self.test_endpoint("PUT", f"/carts/{self.created_ids['cart_id']}", 200, update_data,
                             description="Update cart")

        # Calculate Cart Total
        if self.created_ids['cart_id']:
            self.test_endpoint("PUT", f"/carts/{self.created_ids['cart_id']}/calculate-total", 200,
                             description="Calculate cart total")

    # ==================== CART PRODUCT MANAGEMENT ====================
    def test_cart_product_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING CART PRODUCT MANAGEMENT ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Get All Cart Products
        self.test_endpoint("GET", "/cart-products", 200, description="Get all cart products")

        # Add Product to Cart
        if self.created_ids['cart_id'] and self.created_ids['variant_id']:
            add_data = {
                "variant_id": self.created_ids['variant_id'],
                "cart_id": self.created_ids['cart_id'],
                "quantity": 2
            }
            result = self.test_endpoint("POST", "/cart-products", 201, add_data,
                                       description="Add product to cart")
            if result and 'cart_product_id' in result:
                self.created_ids['cart_product_id'] = result['cart_product_id']

        # Get Cart Product by ID
        if self.created_ids['cart_product_id']:
            self.test_endpoint("GET", f"/cart-products/{self.created_ids['cart_product_id']}", 200,
                             description="Get cart product by ID")

        # Get Cart Products by Cart ID
        if self.created_ids['cart_id']:
            self.test_endpoint("GET", f"/cart-products/cart/{self.created_ids['cart_id']}", 200,
                             description="Get cart products by cart ID")

        # Update Product Quantity in Cart
        if self.created_ids['cart_id'] and self.created_ids['variant_id']:
            update_data = {
                "cart_id": self.created_ids['cart_id'],
                "variant_id": self.created_ids['variant_id'],
                "quantity": 3
            }
            self.test_endpoint("PUT", "/cart-products", 200, update_data,
                             description="Update product quantity in cart")

    # ==================== ORDER MANAGEMENT ====================
    def test_order_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING ORDER MANAGEMENT ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Get All Orders
        self.test_endpoint("GET", "/orders", 200, description="Get all orders")

        # Create Order from Cart (Checkout)
        # Only create order if cart has items (cart_product_id exists means items were added)
        if self.created_ids['cart_id'] and self.created_ids.get('cart_product_id'):
            checkout_data = {
                "address": "123 Test Street, Test City",
                "phone_num": "1234567890",
                "payment_method": "credit_card",
                "order_status": "pending",
                "payment_status": "pending",
                "clear_cart": False
            }
            result = self.test_endpoint("POST", f"/orders/from-cart/{self.created_ids['cart_id']}", 200,
                                       checkout_data, description="Create order from cart (checkout)")
            if result and 'order_id' in result:
                self.created_ids['order_id'] = result['order_id']
        elif self.created_ids['cart_id']:
            # Cart exists but has no items - skip this test with a note
            self.log("\n" + "="*60, Colors.YELLOW)
            self.log("SKIP: Create order from cart - Cart has no items", Colors.YELLOW)
            self.log("="*60, Colors.YELLOW)
            self.results['skipped'] += 1

        # Get Order by ID
        if self.created_ids['order_id']:
            self.test_endpoint("GET", f"/orders/{self.created_ids['order_id']}", 200,
                             description="Get order by ID")

        # Get Orders by Email
        if self.created_ids['account_email']:
            email_encoded = self.created_ids['account_email'].replace('@', '%40')
            self.test_endpoint("GET", f"/orders/user/{email_encoded}", 200,
                             description="Get orders by email")

        # Create Order Manually
        # Note: Manual order creation requires orderItems. Without items, backend returns 400 error.
        # This test verifies the validation works correctly.
        order_data = {
            "email": self.created_ids['account_email'] or "test@test.com",
            "address": "456 Manual Order St",
            "phoneNum": "9876543210",
            "totalPrice": 199.98,
            "orderStatus": "pending",
            "paymentStatus": "pending",
            "paymentMethod": "paypal"
        }
        # Expect 400 Bad Request since orderItems are required but not provided
        result = self.test_endpoint("POST", "/orders", 400, order_data,
                                   description="Create order manually (expected to fail - requires orderItems)")
        # Don't set order_id since this should fail

        # Update Order
        if self.created_ids['order_id']:
            update_data = {"orderStatus": "processing"}
            self.test_endpoint("PUT", f"/orders/{self.created_ids['order_id']}", 200, update_data,
                             description="Update order")

    # ==================== IMAGE MANAGEMENT ====================
    def test_image_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING IMAGE MANAGEMENT ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Get All Images
        self.test_endpoint("GET", "/images", 200, description="Get all images")

        # Create Image (URL-based)
        if self.created_ids['product_id']:
            image_data = {
                "productId": self.created_ids['product_id'],
                "imageUrl": "https://example.com/test-image.jpg",
                "isPrimary": True
            }
            result = self.test_endpoint("POST", "/images", 201, image_data,
                                       description="Create image (URL-based)")
            if result and 'imageId' in result:
                self.created_ids['image_id'] = result['imageId']

        # Get Image by ID
        if self.created_ids['image_id']:
            self.test_endpoint("GET", f"/images/{self.created_ids['image_id']}", 200,
                             description="Get image by ID")

        # Get Images by Product ID
        if self.created_ids['product_id']:
            self.test_endpoint("GET", f"/images/product/{self.created_ids['product_id']}", 200,
                             description="Get images by product ID")

        # Update Image
        if self.created_ids['image_id']:
            update_data = {"isPrimary": False}
            self.test_endpoint("PUT", f"/images/{self.created_ids['image_id']}", 200, update_data,
                             description="Update image")

        # Note: File upload endpoints require multipart/form-data which is harder to test
        # They are skipped here but can be tested manually with Postman

    # ==================== ADMIN COUPON MANAGEMENT ====================
    def test_coupon_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING ADMIN COUPON MANAGEMENT ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Get All Coupons
        self.test_endpoint("GET", "/admin/coupons", 200, description="Get all coupons")

        # Get Active Coupons
        self.test_endpoint("GET", "/admin/coupons/active", 200, description="Get active coupons")

        # Create Coupon
        valid_from = datetime.now().isoformat()
        valid_until = (datetime.now() + timedelta(days=30)).isoformat()
        coupon_data = {
            "couponCode": f"TEST{int(datetime.now().timestamp())}",
            "description": "Test coupon",
            "discountType": "PERCENTAGE",
            "discountValue": 20.00,
            "minPurchaseAmount": 50.00,
            "maxDiscountAmount": 100.00,
            "usageLimit": 100,
            "validFrom": valid_from,
            "validUntil": valid_until,
            "isActive": True
        }
        result = self.test_endpoint("POST", "/admin/coupons", 201, coupon_data,
                                   description="Create coupon")
        if result and 'couponId' in result:
            self.created_ids['coupon_id'] = result['couponId']
            coupon_code = coupon_data['couponCode']

        # Get Coupon by ID
        if self.created_ids['coupon_id']:
            self.test_endpoint("GET", f"/admin/coupons/{self.created_ids['coupon_id']}", 200,
                             description="Get coupon by ID")

        # Get Coupon by Code
        if 'coupon_code' in locals():
            self.test_endpoint("GET", f"/admin/coupons/code/{coupon_code}", 200,
                             description="Get coupon by code")

        # Validate Coupon
        if 'coupon_code' in locals():
            validate_data = {"cart_total": 150.00}
            self.test_endpoint("POST", f"/admin/coupons/validate/{coupon_code}", 200, validate_data,
                             description="Validate coupon")

        # Update Coupon
        if self.created_ids['coupon_id']:
            update_data = {"discountValue": 25.00}
            self.test_endpoint("PUT", f"/admin/coupons/{self.created_ids['coupon_id']}", 200, update_data,
                             description="Update coupon")

    # ==================== ADMIN ACTIVITY MANAGEMENT ====================
    def test_activity_endpoints(self):
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TESTING ADMIN ACTIVITY MANAGEMENT ENDPOINTS", Colors.BOLD)
        self.log("="*60, Colors.BOLD)

        # Get All Activities
        self.test_endpoint("GET", "/admin/activities?page=0&size=20", 200,
                          description="Get all activities (paginated)")

        # Log Activity (Full Object)
        activity_data = {
            "adminEmail": "admin@test.com",
            "actionType": "CREATE",
            "entityType": "PRODUCT",
            "entityId": self.created_ids.get('product_id'),
            "description": "Test activity log"
        }
        result = self.test_endpoint("POST", "/admin/activities", 201, activity_data,
                                   description="Log activity (full object)")
        if result and 'activityId' in result:
            self.created_ids['activity_id'] = result['activityId']

        # Get Activity by ID
        if self.created_ids['activity_id']:
            self.test_endpoint("GET", f"/admin/activities/{self.created_ids['activity_id']}", 200,
                             description="Get activity by ID")

        # Quick Log Activity
        quick_log_data = {
            "admin_email": "admin@test.com",
            "action_type": "UPDATE",
            "entity_type": "ORDER",
            "entity_id": self.created_ids.get('order_id'),
            "description": "Quick test activity"
        }
        self.test_endpoint("POST", "/admin/activities/quick-log", 201, quick_log_data,
                          description="Quick log activity")

        # Get Activities by Admin Email
        self.test_endpoint("GET", "/admin/activities/admin/admin@test.com?page=0&size=20", 200,
                          description="Get activities by admin email")

        # Get Activities by Entity Type
        self.test_endpoint("GET", "/admin/activities/entity/PRODUCT", 200,
                          description="Get activities by entity type")

        # Get Activities by Action Type
        self.test_endpoint("GET", "/admin/activities/action/CREATE", 200,
                          description="Get activities by action type")

    # ==================== RUN ALL TESTS ====================
    def check_server_connection(self):
        """Check if the backend server is running"""
        try:
            # Try to connect to the base URL
            response = self.session.get(f"{self.base_url.replace('/api', '')}/actuator/health", timeout=5)
            return True
        except:
            try:
                # Try a simple GET to /api endpoint
                response = self.session.get(f"{self.base_url}/accounts", timeout=5)
                return True
            except:
                return False

    def run_all_tests(self):
        """Run all endpoint tests"""
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("VICTUSSTORE BACKEND API TEST SUITE", Colors.BOLD)
        self.log("="*60, Colors.BOLD)
        self.log(f"Base URL: {self.base_url}", Colors.BLUE)
        self.log(f"Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}", Colors.BLUE)

        # Check server connection
        self.log("\nChecking server connection...", Colors.BLUE)
        if not self.check_server_connection():
            self.log("\n" + "="*60, Colors.RED)
            self.log("ERROR: Cannot connect to backend server!", Colors.RED)
            self.log("="*60, Colors.RED)
            self.log(f"\nPlease ensure the backend server is running on {self.base_url}", Colors.YELLOW)
            self.log("\nTo start the server:", Colors.YELLOW)
            self.log("  cd victuce", Colors.YELLOW)
            self.log("  mvn spring-boot:run", Colors.YELLOW)
            self.log("\nOr run VictusStoreApplication from your IDE", Colors.YELLOW)
            sys.exit(1)
        else:
            self.log("Server connection OK!", Colors.GREEN)

        try:
            self.test_auth_endpoints()
            self.test_account_endpoints()
            self.test_seller_endpoints()
            self.test_category_endpoints()
            self.test_product_endpoints()
            self.test_variant_endpoints()
            self.test_cart_endpoints()
            self.test_cart_product_endpoints()
            self.test_order_endpoints()
            self.test_image_endpoints()
            self.test_coupon_endpoints()
            self.test_activity_endpoints()
        except KeyboardInterrupt:
            self.log("\n\n⚠️  Tests interrupted by user", Colors.YELLOW)
        except Exception as e:
            self.log(f"\n\n❌ Fatal error: {str(e)}", Colors.RED)
            import traceback
            if self.verbose:
                traceback.print_exc()

        self.print_summary()

    def print_summary(self):
        """Print test summary"""
        self.log("\n" + "="*60, Colors.BOLD)
        self.log("TEST SUMMARY", Colors.BOLD)
        self.log("="*60, Colors.BOLD)
        
        total = self.results['passed'] + self.results['failed'] + self.results['skipped']
        pass_rate = (self.results['passed'] / total * 100) if total > 0 else 0
        
        self.log(f"Total Tests: {total}", Colors.BLUE)
        self.log(f"✅ Passed: {self.results['passed']}", Colors.GREEN)
        self.log(f"❌ Failed: {self.results['failed']}", Colors.RED)
        self.log(f"⏭️  Skipped: {self.results['skipped']}", Colors.YELLOW)
        self.log(f"Pass Rate: {pass_rate:.1f}%", Colors.GREEN if pass_rate >= 80 else Colors.YELLOW)
        
        if self.results['errors']:
            self.log("\n" + "="*60, Colors.BOLD)
            self.log("ERRORS:", Colors.RED)
            for error in self.results['errors'][:10]:  # Show first 10 errors
                self.log(f"  - {error.get('method', '?')} {error.get('endpoint', '?')}: {error.get('error', error.get('response', 'Unknown error'))}", Colors.RED)
            if len(self.results['errors']) > 10:
                self.log(f"  ... and {len(self.results['errors']) - 10} more errors", Colors.YELLOW)

        self.log(f"\nCompleted at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}", Colors.BLUE)
        self.log("="*60, Colors.BOLD)


def main():
    parser = argparse.ArgumentParser(description='Test all VictusStore Backend API endpoints')
    parser.add_argument('--base-url', default='http://localhost:8080/api',
                       help='Base URL of the API (default: http://localhost:8080/api)')
    parser.add_argument('--verbose', '-v', action='store_true',
                       help='Enable verbose output')
    args = parser.parse_args()

    tester = APITester(base_url=args.base_url, verbose=args.verbose)
    tester.run_all_tests()

    # Exit with error code if tests failed
    sys.exit(1 if tester.results['failed'] > 0 else 0)


if __name__ == "__main__":
    main()

