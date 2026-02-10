package com.victusstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victusstore.exception.StockInsufficientException;
import com.victusstore.model.*;
import com.victusstore.repository.*;
import com.victusstore.service.IdempotencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

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

    @Autowired
    private IdempotencyService idempotencyService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @Transactional
    public ResponseEntity<?> createOrderFromCart(
            @PathVariable Long cartId,
            @RequestBody @Valid com.victusstore.dto.CreateOrderRequest orderRequest,
            HttpServletRequest request) {
        
        // Convert DTO to Map for idempotency service
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("address", orderRequest.getAddress());
        orderData.put("phone_num", orderRequest.getPhoneNum());
        orderData.put("payment_method", orderRequest.getPaymentMethod());
        orderData.put("order_status", orderRequest.getOrderStatus());
        orderData.put("payment_status", orderRequest.getPaymentStatus());
        orderData.put("clear_cart", orderRequest.getClearCart());
        
        // Check idempotency key
        String idempotencyKey = request.getHeader("Idempotency-Key");
        String endpoint = "/api/orders/from-cart/" + cartId;
        
        // Get cart and validate
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));

        // Check idempotency if key provided (enforce hash mismatch -> 409)
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            Optional<String> cachedResponse = idempotencyService.getCachedResponseOrThrowOnMismatch(
                    idempotencyKey, cart.getEmail(), endpoint, orderData);
            if (cachedResponse.isPresent()) {
                try {
                    Map<String, Object> response = objectMapper.readValue(
                            cachedResponse.get(), Map.class);
                    logger.info("Returning cached response for idempotency key: {}", idempotencyKey);
                    return ResponseEntity.ok(response);
                } catch (Exception e) {
                    logger.warn("Failed to parse cached response: {}", e.getMessage());
                }
            }
        }

        // Get cart products
        List<CartProduct> cartProducts = cartProductRepository.findByCartId(cartId);
        if (cartProducts.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Get account
        Account account = accountRepository.findByEmail(cart.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Validate stock with pessimistic locking and calculate total
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<Map<String, Object>> orderItemsDetails = new ArrayList<>();
        List<String> stockIssues = new ArrayList<>();

        for (CartProduct cartProduct : cartProducts) {
            try {
                // Lock variant row for update
                ProductVariant variant = variantRepository.findByIdWithLock(cartProduct.getVariantId())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Product variant not found for cart item: " + cartProduct.getVariantId()));

                int availableStock = 0;
                try {
                    availableStock = variant.getStockQuantity();
                } catch (Exception e) {
                    logger.error("Could not get stock quantity for variant {}: {}", 
                            variant.getVariantId(), e.getMessage());
                    throw new IllegalArgumentException("Unable to verify stock for product variant: " + variant.getVariantId());
                }
                
                int requestedQuantity = 0;
                try {
                    requestedQuantity = cartProduct.getQuantity();
                } catch (Exception e) {
                    logger.error("Could not get quantity for cart product {}: {}", 
                            cartProduct.getId(), e.getMessage());
                    throw new IllegalArgumentException("Unable to verify quantity for cart item: " + cartProduct.getId());
                }

                // Validate stock - prevent going below zero
                if (availableStock < requestedQuantity) {
                    String productName = "Unknown Product";
                    String variantDetails = "Unknown Variant";
                    
                    try {
                        if (variant.getProduct() != null) {
                            productName = variant.getProduct().getProductName();
                            variantDetails = variant.getColor() + " - " + variant.getSize();
                        }
                    } catch (Exception e) {
                        logger.debug("Could not get product details: {}", e.getMessage());
                    }
                    
                    String stockIssue = String.format(
                        "Insufficient stock for %s (%s). Available: %d, Requested: %d",
                        productName, variantDetails, availableStock, requestedQuantity
                    );
                    stockIssues.add(stockIssue);
                    
                    // Continue checking other items to give complete error information
                    continue;
                }

                // Calculate item total
                BigDecimal itemTotal = BigDecimal.ZERO;
                try {
                    itemTotal = cartProduct.getPriceAtTime()
                            .multiply(BigDecimal.valueOf(requestedQuantity));
                } catch (Exception e) {
                    logger.error("Could not calculate item total: {}", e.getMessage());
                    throw new IllegalArgumentException("Unable to calculate price for cart item: " + cartProduct.getId());
                }
                
                totalPrice = totalPrice.add(itemTotal);

                // Prepare order item details
                Map<String, Object> itemDetail = new HashMap<>();
                itemDetail.put("variant_id", cartProduct.getVariantId());
                itemDetail.put("quantity", requestedQuantity);
                itemDetail.put("price_at_time", cartProduct.getPriceAtTime());
                
                try {
                    if (variant.getProduct() != null) {
                        itemDetail.put("product_name", variant.getProduct().getProductName());
                        itemDetail.put("variant_details", variant.getColor() + " - " + variant.getSize());
                    } else {
                        itemDetail.put("product_name", "Unknown");
                        itemDetail.put("variant_details", "Unknown");
                    }
                } catch (Exception e) {
                    logger.debug("Could not extract product details: {}", e.getMessage());
                    itemDetail.put("product_name", "Unknown");
                    itemDetail.put("variant_details", "Unknown");
                }
                
                orderItemsDetails.add(itemDetail);
                
            } catch (Exception e) {
                logger.error("Error processing cart product {}: {}", cartProduct.getId(), e.getMessage());
                throw new IllegalArgumentException("Error processing cart item: " + e.getMessage());
            }
        }

        // If there are stock issues, return detailed error
        if (!stockIssues.isEmpty()) {
            throw new StockInsufficientException(
                "Stock validation failed for one or more items:\n" + String.join("\n", stockIssues),
                null, null, null);
        }

        // Ensure we have at least one valid item
        if (orderItemsDetails.isEmpty()) {
            throw new IllegalArgumentException("No valid items available for order");
        }

        // Create order
        Order order = new Order();
        order.setEmail(cart.getEmail());
        order.setAddress(orderRequest.getAddress() != null ? orderRequest.getAddress() : "");
        String phoneNum = orderRequest.getPhoneNum() != null ? orderRequest.getPhoneNum() :
                (account.getPhoneNum() != null ? account.getPhoneNum() : "");
        order.setPhoneNum(phoneNum);
        order.setTotalPrice(totalPrice);
        order.setOrderStatus(orderRequest.getOrderStatus() != null ? orderRequest.getOrderStatus() : "pending");
        order.setPaymentStatus(orderRequest.getPaymentStatus() != null ? orderRequest.getPaymentStatus() : "pending");
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setOrderDate(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Update stock quantities and link cart products to order
        for (CartProduct cartProduct : cartProducts) {
            // Lock variant again to ensure consistency
            ProductVariant variant = variantRepository.findByIdWithLock(cartProduct.getVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Product variant not found"));

            // Double-check stock before updating
            int availableStock = variant.getStockQuantity();
            int requestedQuantity = cartProduct.getQuantity();
            
            if (availableStock < requestedQuantity) {
                throw new StockInsufficientException(
                        "Stock changed during processing for variant " + variant.getVariantId(),
                        variant.getVariantId(),
                        availableStock,
                        requestedQuantity);
            }

            // Update stock
            int newStock = availableStock - requestedQuantity;
            variant.setStockQuantity(newStock);
            variant.setUpdatedAt(LocalDateTime.now());
            variantRepository.save(variant);

            // Link cart product to order
            cartProduct.setOrderId(savedOrder.getOrderId());
            cartProductRepository.save(cartProduct);
        }

        // Update cart (optional - mark cart as inactive)
        if (orderRequest.getClearCart() != null && orderRequest.getClearCart()) {
            cart.setIsActive(false);
            cartRepository.save(cart);
        }

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order created successfully");
        response.put("order_id", savedOrder.getOrderId());
        response.put("total_price", savedOrder.getTotalPrice());
        response.put("order_status", savedOrder.getOrderStatus());
        response.put("order_items", orderItemsDetails);

        // Store idempotency key if provided
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            idempotencyService.storeResponse(
                    idempotencyKey, cart.getEmail(), endpoint, orderData, response);
        }

        logger.info("Order created successfully: orderId={}, cartId={}, totalPrice={}", 
                savedOrder.getOrderId(), cartId, totalPrice);
        
        return ResponseEntity.ok(response);
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
