package com.victusstore.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminActivity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;
    
    @Column(name = "admin_email", nullable = false, length = 255)
    private String adminEmail;
    
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType; // "CREATE", "UPDATE", "DELETE", "VIEW", etc.
    
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType; // "PRODUCT", "ORDER", "COUPON", "USER", etc.
    
    @Column(name = "entity_id")
    private Long entityId;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

