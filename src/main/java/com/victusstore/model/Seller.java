package com.victusstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sellers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "seller_name", nullable = false, length = 255)
    private String sellerName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "email", referencedColumnName = "email", insertable = false, updatable = false)
    @JsonIgnore
    private Account account;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Product> products;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
