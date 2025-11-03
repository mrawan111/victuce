package com.victusstore.controller;

import com.victusstore.model.*;
import com.victusstore.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartProductRepository cartProductRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderRepository.findAllOrdersWithItems();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/with-products")
    public ResponseEntity<List<Order>> getAllOrdersWithProducts() {
        List<Order> orders = orderRepository.findAllOrdersWithItems();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderRepository.findByIdWithItems(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<Order>> getOrdersByEmail(@PathVariable String email) {
        List<Order> orders = orderRepository.findOrdersByEmailWithItems(email);
        return ResponseEntity.ok(orders);
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        // Validation: Orders must have items
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Orders must have at least one item. Use /api/orders/from-cart/{cartId} to create orders from cart."));
        }

        // Save the order first to get the orderId
        Order savedOrder = orderRepository.save(order);

        // Link cart products to the order
        for (CartProduct cartProduct : order.getOrderItems()) {
            cartProduct.setOrderId(savedOrder.getOrderId());
            cartProductRepository.save(cartProduct);
        }

        return ResponseEntity.ok(savedOrder);
    }

    @PostMapping("/from-cart/{cartId}")
    public ResponseEntity<?> createOrderFromCart(
            @PathVariable Long cartId,
            @RequestBody Map<String, Object> orderData) {
        try {
            // Get cart
            Cart cart = cartRepository.findById(cartId)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            // Get cart products
            List<CartProduct> cartProducts = cartProductRepository.findByCartId(cartId);
            if (cartProducts.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
            }

            // Validate stock availability and calculate total
            BigDecimal totalPrice = BigDecimal.ZERO;
            for (CartProduct cartProduct : cartProducts) {
                ProductVariant variant = variantRepository.findById(cartProduct.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Product variant not found for cart item"));
                
                if (variant.getStockQuantity() < cartProduct.getQuantity()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Insufficient stock for variant: " + variant.getVariantId(),
                            "variant_id", variant.getVariantId(),
                            "available_stock", variant.getStockQuantity(),
                            "requested_quantity", cartProduct.getQuantity()
                    ));
                }
                
                // Calculate total (quantity * price_at_time)
                BigDecimal itemTotal = cartProduct.getPriceAtTime()
                        .multiply(BigDecimal.valueOf(cartProduct.getQuantity()));
                totalPrice = totalPrice.add(itemTotal);
            }

            // Get account for phone number if not provided
            Account account = accountRepository.findByEmail(cart.getEmail())
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            // Create order
            Order order = new Order();
            order.setEmail(cart.getEmail());
            order.setAddress((String) orderData.getOrDefault("address", ""));
            order.setPhoneNum((String) orderData.getOrDefault("phone_num", account.getPhoneNum()));
            order.setTotalPrice(totalPrice);
            order.setOrderStatus((String) orderData.getOrDefault("order_status", "pending"));
            order.setPaymentStatus((String) orderData.getOrDefault("payment_status", "pending"));
            order.setPaymentMethod((String) orderData.get("payment_method"));
            order.setOrderDate(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());

            Order savedOrder = orderRepository.save(order);

            // Update stock quantities and link cart products to order
            for (CartProduct cartProduct : cartProducts) {
                ProductVariant variant = variantRepository.findById(cartProduct.getVariantId())
                        .orElseThrow(() -> new RuntimeException("Product variant not found"));

                // Update stock
                int newStock = variant.getStockQuantity() - cartProduct.getQuantity();
                variant.setStockQuantity(newStock);
                variant.setUpdatedAt(LocalDateTime.now());
                variantRepository.save(variant);

                // Link cart product to order
                cartProduct.setOrderId(savedOrder.getOrderId());
                cartProductRepository.save(cartProduct);
            }

            // Update cart total (optional - mark cart as inactive or clear it)
            if (orderData.containsKey("clear_cart") && (Boolean) orderData.get("clear_cart")) {
                cart.setIsActive(false);
                cartRepository.save(cart);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order created successfully");
            response.put("order_id", savedOrder.getOrderId());
            response.put("total_price", savedOrder.getTotalPrice());
            response.put("order_status", savedOrder.getOrderStatus());

            // Include order items with product details for verification
            List<Map<String, Object>> orderItemsDetails = new ArrayList<>();
            for (CartProduct cartProduct : cartProducts) {
                ProductVariant variant = variantRepository.findById(cartProduct.getVariantId()).orElse(null);
                Map<String, Object> itemDetail = new HashMap<>();
                itemDetail.put("variant_id", cartProduct.getVariantId());
                itemDetail.put("quantity", cartProduct.getQuantity());
                itemDetail.put("price_at_time", cartProduct.getPriceAtTime());
                if (variant != null && variant.getProduct() != null) {
                    itemDetail.put("product_name", variant.getProduct().getProductName());
                    itemDetail.put("variant_details", variant.getColor() + " - " + variant.getSize());
                } else {
                    itemDetail.put("product_name", "Unknown");
                    itemDetail.put("variant_details", "Unknown");
                }
                orderItemsDetails.add(itemDetail);
            }
            response.put("order_items", orderItemsDetails);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to create order: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order orderDetails) {
        Order order = orderRepository.findByIdWithItems(id);
        if (order != null) {
            if (orderDetails.getTotalPrice() != null) order.setTotalPrice(orderDetails.getTotalPrice());
            if (orderDetails.getOrderStatus() != null) order.setOrderStatus(orderDetails.getOrderStatus());
            if (orderDetails.getAddress() != null) order.setAddress(orderDetails.getAddress());
            Order updatedOrder = orderRepository.save(order);
            return ResponseEntity.ok(updatedOrder);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    orderRepository.delete(order);
                    Map<String, Boolean> response = new HashMap<>();
                    response.put("deleted", Boolean.TRUE);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
