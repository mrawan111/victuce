package com.victusstore.repository;

import com.victusstore.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    
    Optional<Region> findByRegionCode(String regionCode);
    
    boolean existsByRegionCode(String regionCode);
}
