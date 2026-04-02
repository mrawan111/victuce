package com.victusstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity for Egypt cities
 */
@Entity
@Table(name = "cities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "region_code", nullable = false, length = 10)
    private String regionCode;
    
    @Column(name = "is_other", nullable = false)
    private Boolean isOther = false;
}
