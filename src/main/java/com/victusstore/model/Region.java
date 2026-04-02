package com.victusstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA Entity for Egypt regions
 */
@Entity
@Table(name = "regions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "region_code", unique = true, nullable = false, length = 10)
    private String regionCode;
}
