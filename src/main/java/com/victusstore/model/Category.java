package com.victusstore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;
    
    @Column(name = "category_name", nullable = false, unique = true, length = 255)
    private String categoryName;
    
    @Column(name = "category_image", length = 255)
    private String categoryImage;
    
    @Column(name = "parent_category_id")
    private Long parentCategoryId;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "category")
    private List<Product> products;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}