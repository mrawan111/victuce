package com.victusstore.controller;

import com.victusstore.model.Cart;
import com.victusstore.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

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

    @PostMapping
    public ResponseEntity<Cart> createCart(@RequestBody Cart cart) {
        Cart savedCart = cartRepository.save(cart);
        return ResponseEntity.ok(savedCart);
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
}
