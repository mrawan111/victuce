package com.victusstore.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "product_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(nullable = false, length = 50)
    private String color;
    
    @Column(nullable = false, length = 50)
    private String size;
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(length = 50, unique = true)
    private String sku;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
    
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL)
    private List<Image> images;
    
    @OneToMany(mappedBy = "variant")
    private List<CartProduct> cartProducts;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}