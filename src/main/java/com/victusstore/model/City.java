package com.victusstore.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "cities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class City {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "region_code", nullable = false)
    private String regionCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_code", referencedColumnName = "regionCode", insertable = false, updatable = false)
    private Region region;
}
