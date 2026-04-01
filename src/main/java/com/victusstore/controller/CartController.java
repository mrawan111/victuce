package com.victusstore.controller;

import com.victusstore.model.Account;
import com.victusstore.model.Cart;
import com.victusstore.model.CartProduct;
import com.victusstore.repository.AccountRepository;
import com.victusstore.repository.CartRepository;
import com.victusstore.repository.CartProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartProductRepository cartProductRepository;

    @Autowired
    private AccountRepository accountRepository;

    @GetMapping
    public ResponseEntity<List<Cart>> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();
        return ResponseEntity.ok(carts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cart> getCartById(@PathVariable Long id) {
        return cartRepository.findById(id)
                .map(cart -> ResponseEntity.ok(cart))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<Cart> getCartByEmail(@PathVariable String email) {
        List<Cart> carts = cartRepository.findAllByAccount_Email(email);
        if (carts == null || carts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Cart cart = resolvePrimaryCart(carts);
        return ResponseEntity.ok(cart);
    }

    @PostMapping
    public ResponseEntity<?> createCart(@RequestBody Cart cart) {
        try {
            // Validate that the account exists
            Optional<Account> accountOpt = accountRepository.findByEmail(cart.getEmail());
            if (accountOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Account with email " + cart.getEmail() + " does not exist"));
            }

            Cart savedCart = cartRepository.save(cart);
            return ResponseEntity.ok(savedCart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cart> updateCart(@PathVariable Long id, @RequestBody Cart cartDetails) {
        return cartRepository.findById(id)
                .map(cart -> {
                    cart.setTotalPrice(cartDetails.getTotalPrice());
                    cart.setIsActive(cartDetails.getIsActive());
                    Cart updatedCart = cartRepository.save(cart);
                    return ResponseEntity.ok(updatedCart);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteCart(@PathVariable Long id) {
        return cartRepository.findById(id)
                .map(cart -> {
                    cartRepository.delete(cart);
                    Map<String, Boolean> response = new HashMap<>();
                    response.put("deleted", Boolean.TRUE);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncCart(@RequestBody Map<String, Object> data) {
        try {
            String email = (String) data.get("email");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            // Validate that the account exists
            Optional<Account> accountOpt = accountRepository.findByEmail(email);
            if (accountOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Account with email " + email + " does not exist"));
            }

            // Get or create a canonical cart for the user. If duplicates already exist
            // in the database, merge them instead of failing with a non-unique result.
            List<Cart> carts = cartRepository.findAllByAccount_Email(email);
            Cart cart;
            if (carts != null && !carts.isEmpty()) {
                cart = mergeDuplicateCarts(carts);
            } else {
                cart = new Cart();
                cart.setEmail(email);
                cart.setTotalPrice(BigDecimal.ZERO);
                cart.setIsActive(true);
                cart = cartRepository.save(cart);
            }

            // Calculate total from cart products
            List<CartProduct> cartProducts = cartProductRepository.findByCartId(cart.getCartId());
            BigDecimal totalPrice = BigDecimal.ZERO;
            for (CartProduct cartProduct : cartProducts) {
                BigDecimal itemTotal = cartProduct.getPriceAtTime()
                        .multiply(BigDecimal.valueOf(cartProduct.getQuantity()));
                totalPrice = totalPrice.add(itemTotal);
            }

            // Update cart total
            cart.setTotalPrice(totalPrice);
            cart = cartRepository.save(cart);

            Map<String, Object> response = new HashMap<>();
            response.put("cart_id", cart.getCartId());
            response.put("email", cart.getEmail());
            response.put("total_price", cart.getTotalPrice());
            response.put("item_count", cartProducts.size());
            response.put("synced", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/calculate-total")
    public ResponseEntity<?> calculateCartTotal(@PathVariable Long id) {
        try {
            Optional<Cart> cartOpt = cartRepository.findById(id);
            if (!cartOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("error", "Cart not found"));
            }

            Cart cart = cartOpt.get();
            List<CartProduct> cartProducts = cartProductRepository.findByCartId(id);

            BigDecimal totalPrice = BigDecimal.ZERO;
            for (CartProduct cartProduct : cartProducts) {
                BigDecimal itemTotal = cartProduct.getPriceAtTime()
                        .multiply(BigDecimal.valueOf(cartProduct.getQuantity()));
                totalPrice = totalPrice.add(itemTotal);
            }

            cart.setTotalPrice(totalPrice);
            cart = cartRepository.save(cart);

            Map<String, Object> response = new HashMap<>();
            response.put("cart_id", cart.getCartId());
            response.put("total_price", cart.getTotalPrice());
            response.put("item_count", cartProducts.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Cart resolvePrimaryCart(List<Cart> carts) {
        return carts.stream()
                .sorted(
                        Comparator
                                .comparing((Cart cart) -> Boolean.TRUE.equals(cart.getIsActive()) ? 0 : 1)
                                .thenComparing(
                                        Cart::getUpdatedAt,
                                        Comparator.nullsLast(Comparator.reverseOrder())
                                )
                                .thenComparing(
                                        Cart::getCreatedAt,
                                        Comparator.nullsLast(Comparator.reverseOrder())
                                )
                                .thenComparing(
                                        Cart::getCartId,
                                        Comparator.nullsLast(Comparator.reverseOrder())
                                )
                )
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No cart found"));
    }

    private Cart mergeDuplicateCarts(List<Cart> carts) {
        Cart primaryCart = resolvePrimaryCart(carts);
        Map<Long, CartProduct> mergedByVariant = new LinkedHashMap<>();

        for (Cart currentCart : carts) {
            List<CartProduct> items = cartProductRepository.findByCartId(currentCart.getCartId());
            for (CartProduct item : items) {
                CartProduct existing = mergedByVariant.get(item.getVariantId());
                if (existing == null) {
                    item.setCartId(primaryCart.getCartId());
                    mergedByVariant.put(item.getVariantId(), item);
                    continue;
                }

                existing.setQuantity(existing.getQuantity() + item.getQuantity());
                if (item.getPriceAtTime() != null) {
                    existing.setPriceAtTime(item.getPriceAtTime());
                }
                cartProductRepository.deleteById(item.getId());
            }

            if (!primaryCart.getCartId().equals(currentCart.getCartId())) {
                currentCart.setIsActive(false);
                currentCart.setTotalPrice(BigDecimal.ZERO);
                cartRepository.save(currentCart);
            }
        }

        for (CartProduct mergedItem : mergedByVariant.values()) {
            if (!primaryCart.getCartId().equals(mergedItem.getCartId())) {
                mergedItem.setCartId(primaryCart.getCartId());
            }
            cartProductRepository.save(mergedItem);
        }

        primaryCart.setIsActive(true);
        return cartRepository.save(primaryCart);
    }
}
