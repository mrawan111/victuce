package com.victusstore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "variant_id", nullable = false)
    private Long variantId;

    @Column(name = "cart_id", nullable = false)
    private Long cartId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_at_time", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtTime;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "variant_id", insertable = false, updatable = false)
    private ProductVariant variant;

    @ManyToOne
    @JoinColumn(name = "cart_id", insertable = false, updatable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
