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
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/carts")
@CrossOrigin(origins = "*")
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
        return cartRepository.findByAccount_Email(email)
                .map(cart -> ResponseEntity.ok(cart))
                .orElse(ResponseEntity.notFound().build());
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

            // Get or create cart for user
            Optional<Cart> cartOpt = cartRepository.findByAccount_Email(email);
            Cart cart;
            if (cartOpt.isPresent()) {
                cart = cartOpt.get();
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
}
