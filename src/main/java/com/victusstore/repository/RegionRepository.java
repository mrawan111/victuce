package com.victusstore.repository;

import com.victusstore.model.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for Region entity
 */
@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {
    
    /**
     * Find region by region code
     */
    Optional<Region> findByRegionCode(String regionCode);
    
    /**
     * Check if region code exists
     */
    boolean existsByRegionCode(String regionCode);
    
    /**
     * Get all regions ordered by name
     */
    @Query("SELECT r FROM Region r ORDER BY r.name ASC")
    List<Region> findAllOrderByName();
}
