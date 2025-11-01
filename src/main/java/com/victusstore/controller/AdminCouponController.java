package com.victusstore.controller;

import com.victusstore.model.Coupon;
import com.victusstore.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/coupons")
@CrossOrigin(origins = "*")
public class AdminCouponController {

    @Autowired
    private CouponRepository couponRepository;

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        List<Coupon> coupons = couponRepository.findAll();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Coupon>> getActiveCoupons() {
        List<Coupon> coupons = couponRepository.findByIsActive(true);
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCouponById(@PathVariable Long id) {
        Optional<Coupon> coupon = couponRepository.findById(id);
        if (coupon.isPresent()) {
            return ResponseEntity.ok(coupon.get());
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Coupon not found"));
        }
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<?> getCouponByCode(@PathVariable String code) {
        Optional<Coupon> coupon = couponRepository.findByCouponCode(code.toUpperCase());
        if (coupon.isPresent()) {
            return ResponseEntity.ok(coupon.get());
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Coupon not found"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCoupon(@RequestBody Coupon coupon) {
        try {
            if (coupon.getCouponCode() == null || coupon.getCouponCode().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Coupon code is required"));
            }
            
            // Check if coupon code already exists
            if (couponRepository.findByCouponCode(coupon.getCouponCode().toUpperCase()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Coupon code already exists"));
            }
            
            // Validate discount type
            if (coupon.getDiscountType() == null || 
                (!coupon.getDiscountType().equals("PERCENTAGE") && !coupon.getDiscountType().equals("FIXED"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "Discount type must be 'PERCENTAGE' or 'FIXED'"));
            }
            
            // Validate dates
            if (coupon.getValidFrom() != null && coupon.getValidUntil() != null) {
                if (coupon.getValidUntil().isBefore(coupon.getValidFrom())) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Valid until date must be after valid from date"));
                }
            }
            
            coupon.setCouponCode(coupon.getCouponCode().toUpperCase());
            if (coupon.getUsedCount() == null) {
                coupon.setUsedCount(0);
            }
            
            Coupon savedCoupon = couponRepository.save(coupon);
            return ResponseEntity.status(201).body(savedCoupon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoupon(@PathVariable Long id, @RequestBody Coupon couponDetails) {
        try {
            Optional<Coupon> couponOpt = couponRepository.findById(id);
            if (couponOpt.isPresent()) {
                Coupon coupon = couponOpt.get();
                
                if (couponDetails.getCouponCode() != null && !couponDetails.getCouponCode().isEmpty()) {
                    // Check if new code conflicts with existing coupon
                    Optional<Coupon> existing = couponRepository.findByCouponCode(couponDetails.getCouponCode().toUpperCase());
                    if (existing.isPresent() && !existing.get().getCouponId().equals(id)) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Coupon code already exists"));
                    }
                    coupon.setCouponCode(couponDetails.getCouponCode().toUpperCase());
                }
                
                if (couponDetails.getDescription() != null) {
                    coupon.setDescription(couponDetails.getDescription());
                }
                if (couponDetails.getDiscountType() != null) {
                    if (!couponDetails.getDiscountType().equals("PERCENTAGE") && !couponDetails.getDiscountType().equals("FIXED")) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Discount type must be 'PERCENTAGE' or 'FIXED'"));
                    }
                    coupon.setDiscountType(couponDetails.getDiscountType());
                }
                if (couponDetails.getDiscountValue() != null) {
                    coupon.setDiscountValue(couponDetails.getDiscountValue());
                }
                if (couponDetails.getMinPurchaseAmount() != null) {
                    coupon.setMinPurchaseAmount(couponDetails.getMinPurchaseAmount());
                }
                if (couponDetails.getMaxDiscountAmount() != null) {
                    coupon.setMaxDiscountAmount(couponDetails.getMaxDiscountAmount());
                }
                if (couponDetails.getUsageLimit() != null) {
                    coupon.setUsageLimit(couponDetails.getUsageLimit());
                }
                if (couponDetails.getValidFrom() != null) {
                    coupon.setValidFrom(couponDetails.getValidFrom());
                }
                if (couponDetails.getValidUntil() != null) {
                    coupon.setValidUntil(couponDetails.getValidUntil());
                }
                if (couponDetails.getIsActive() != null) {
                    coupon.setIsActive(couponDetails.getIsActive());
                }
                
                // Validate dates
                if (coupon.getValidFrom() != null && coupon.getValidUntil() != null) {
                    if (coupon.getValidUntil().isBefore(coupon.getValidFrom())) {
                        return ResponseEntity.badRequest().body(Map.of("error", "Valid until date must be after valid from date"));
                    }
                }
                
                Coupon updatedCoupon = couponRepository.save(coupon);
                return ResponseEntity.ok(updatedCoupon);
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "Coupon not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCoupon(@PathVariable Long id) {
        try {
            if (couponRepository.existsById(id)) {
                couponRepository.deleteById(id);
                return ResponseEntity.ok(Map.of("message", "Coupon deleted successfully"));
            } else {
                return ResponseEntity.status(404).body(Map.of("message", "Coupon not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/validate/{code}")
    public ResponseEntity<?> validateCoupon(@PathVariable String code, @RequestBody Map<String, Object> data) {
        try {
            BigDecimal cartTotal = new BigDecimal(data.getOrDefault("cart_total", "0").toString());
            Optional<Coupon> couponOpt = couponRepository.findByCouponCode(code.toUpperCase());
            
            if (!couponOpt.isPresent()) {
                return ResponseEntity.status(404).body(Map.of("valid", false, "error", "Coupon not found"));
            }
            
            Coupon coupon = couponOpt.get();
            
            // Check if coupon is active
            if (!coupon.getIsActive()) {
                return ResponseEntity.ok(Map.of("valid", false, "error", "Coupon is not active"));
            }
            
            // Check validity dates
            LocalDateTime now = LocalDateTime.now();
            if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
                return ResponseEntity.ok(Map.of("valid", false, "error", "Coupon is not yet valid"));
            }
            if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
                return ResponseEntity.ok(Map.of("valid", false, "error", "Coupon has expired"));
            }
            
            // Check usage limit
            if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
                return ResponseEntity.ok(Map.of("valid", false, "error", "Coupon usage limit reached"));
            }
            
            // Check minimum purchase amount
            if (coupon.getMinPurchaseAmount() != null && cartTotal.compareTo(coupon.getMinPurchaseAmount()) < 0) {
                return ResponseEntity.ok(Map.of(
                    "valid", false, 
                    "error", "Minimum purchase amount not met",
                    "minimum_amount", coupon.getMinPurchaseAmount()
                ));
            }
            
            // Calculate discount
            BigDecimal discount = BigDecimal.ZERO;
            if (coupon.getDiscountType().equals("PERCENTAGE")) {
                discount = cartTotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
                if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                    discount = coupon.getMaxDiscountAmount();
                }
            } else {
                discount = coupon.getDiscountValue();
                if (discount.compareTo(cartTotal) > 0) {
                    discount = cartTotal;
                }
            }
            
            BigDecimal finalAmount = cartTotal.subtract(discount);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }
            
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "coupon_code", coupon.getCouponCode(),
                "discount", discount,
                "discount_type", coupon.getDiscountType(),
                "original_amount", cartTotal,
                "final_amount", finalAmount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", e.getMessage()));
        }
    }
}

