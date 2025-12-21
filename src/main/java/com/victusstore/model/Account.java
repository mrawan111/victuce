package com.victusstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(length = 255)
    private String email;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "phone_num", length = 15)
    private String phoneNum;

    @Column(name = "seller_account")
    private Boolean sellerAccount = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "role", length = 20)
    private String role = "CUSTOMER"; // CUSTOMER, SELLER, ADMIN

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Seller> sellers;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Cart> carts;

    @OneToMany(mappedBy = "account")
    @JsonIgnore
    private List<Order> orders;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
