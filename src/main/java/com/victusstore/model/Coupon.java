package com.victusstore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;
    
    @Column(name = "coupon_code", nullable = false, unique = true, length = 50)
    private String couponCode;
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType; // "PERCENTAGE" or "FIXED"
    
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "min_purchase_amount", precision = 10, scale = 2)
    private BigDecimal minPurchaseAmount;
    
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;
    
    @Column(name = "usage_limit")
    private Integer usageLimit;
    
    @Column(name = "used_count")
    @Builder.Default
    private Integer usedCount = 0;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (usedCount == null) {
            usedCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

