package com.victusstore.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victusstore.model.*;
import com.victusstore.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartProductRepository cartProductRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductVariantRepository variantRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Account testAccount;
    private Cart testCart;
    private Product testProduct;
    private ProductVariant testVariant;

    @BeforeEach
    void setUp() {
        // Create test account
        testAccount = Account.builder()
                .email("test@example.com")
                .password("password123")
                .role("CUSTOMER")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        testAccount = accountRepository.save(testAccount);

        // Create test product
        testProduct = Product.builder()
                .productName("Test Product")
                .basePrice(new BigDecimal("99.99"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        testProduct = productRepository.save(testProduct);

        // Create test variant with stock
        testVariant = ProductVariant.builder()
                .productId(testProduct.getProductId())
                .color("Red")
                .size("M")
                .stockQuantity(10)
                .price(new BigDecimal("9.99"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        testVariant = variantRepository.save(testVariant);

        // Create test cart
        testCart = Cart.builder()
                .email(testAccount.getEmail())
                .totalPrice(BigDecimal.ZERO)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        testCart = cartRepository.save(testCart);

        // Add product to cart
        CartProduct cartProduct = CartProduct.builder()
                .cartId(testCart.getCartId())
                .variantId(testVariant.getVariantId())
                .quantity(2)
                .priceAtTime(new BigDecimal("9.99"))
                .createdAt(LocalDateTime.now())
                .build();
        cartProductRepository.save(cartProduct);
    }

    @Test
    void testIdempotency_SameKeyReturnsSameOrderId() throws Exception {
        String idempotencyKey = UUID.randomUUID().toString();
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("address", "123 Test St");
        orderData.put("phone_num", "1234567890");
        orderData.put("clear_cart", false);

        // First request
        String response1 = mockMvc.perform(post("/api/orders/from-cart/" + testCart.getCartId())
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderData)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> result1 = objectMapper.readValue(response1, Map.class);
        Long orderId1 = ((Number) result1.get("order_id")).longValue();

        // Second request with same key
        String response2 = mockMvc.perform(post("/api/orders/from-cart/" + testCart.getCartId())
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderData)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Map<String, Object> result2 = objectMapper.readValue(response2, Map.class);
        Long orderId2 = ((Number) result2.get("order_id")).longValue();

        // Verify same order ID returned
        assertEquals(orderId1, orderId2, "Idempotency key should return same order ID");

        // Verify only one order was created
        long orderCount = orderRepository.count();
        assertEquals(1, orderCount, "Only one order should be created with idempotency");
    }

    @Test
    void testConcurrentCheckout_StockNeverNegative() throws Exception {
        // Set initial stock to 5
        testVariant.setStockQuantity(5);
        variantRepository.save(testVariant);

        // Create multiple carts with same variant
        Cart cart1 = createCartWithVariant(testVariant, 3);
        Cart cart2 = createCartWithVariant(testVariant, 3);
        Cart cart3 = createCartWithVariant(testVariant, 3);

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("address", "123 Test St");
        orderData.put("phone_num", "1234567890");

        // Simulate concurrent requests (in real scenario, these would be parallel)
        // First request should succeed (3 items, stock becomes 2)
        mockMvc.perform(post("/api/orders/from-cart/" + cart1.getCartId())
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderData)))
                .andExpect(status().isOk());

        // Second request should fail (only 2 left, requesting 3)
        mockMvc.perform(post("/api/orders/from-cart/" + cart2.getCartId())
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("STOCK_INSUFFICIENT"));

        // Verify stock is not negative
        ProductVariant updatedVariant = variantRepository.findById(testVariant.getVariantId()).orElseThrow();
        assertTrue(updatedVariant.getStockQuantity() >= 0, "Stock should never be negative");
        assertEquals(2, updatedVariant.getStockQuantity(), "Stock should be 2 after first order");
    }

    private Cart createCartWithVariant(ProductVariant variant, int quantity) {
        Cart cart = Cart.builder()
                .email(testAccount.getEmail())
                .totalPrice(BigDecimal.ZERO)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        cart = cartRepository.save(cart);

        CartProduct cartProduct = CartProduct.builder()
                .cartId(cart.getCartId())
                .variantId(variant.getVariantId())
                .quantity(quantity)
                .priceAtTime(variant.getPrice())
                .createdAt(LocalDateTime.now())
                .build();
        cartProductRepository.save(cartProduct);

        return cart;
    }
}

