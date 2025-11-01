package com.victusstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "phone_num", nullable = false, length = 15)
    private String phoneNum;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "order_status", nullable = false, length = 20)
    private String orderStatus = "pending";

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus = "pending";

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "email", referencedColumnName = "email", insertable = false, updatable = false)
    @JsonIgnore
    private Account account;

    @OneToMany(mappedBy = "order")
    private List<CartProduct> orderItems;

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
