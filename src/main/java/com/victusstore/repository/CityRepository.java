package com.victusstore.repository;

import com.victusstore.model.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for City entity
 */
@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    
    /**
     * Find cities by region code ordered by name
     */
    @Query("SELECT c FROM City c WHERE c.regionCode = :regionCode ORDER BY c.name ASC")
    List<City> findByRegionCodeOrderByName(@Param("regionCode") String regionCode);
    
    /**
     * Find city by name and region code
     */
    Optional<City> findByNameAndRegionCode(String name, String regionCode);
    
    /**
     * Check if city exists by name and region code
     */
    boolean existsByNameAndRegionCode(String name, String regionCode);
    
    /**
     * Find "Other" city for a region
     */
    @Query("SELECT c FROM City c WHERE c.regionCode = :regionCode AND c.isOther = true")
    Optional<City> findOtherCityByRegionCode(@Param("regionCode") String regionCode);
    
    /**
     * Find all cities that are not "Other" for a region
     */
    @Query("SELECT c FROM City c WHERE c.regionCode = :regionCode AND c.isOther = false ORDER BY c.name ASC")
    List<City> findNonOtherCitiesByRegionCode(@Param("regionCode") String regionCode);
    
    /**
     * Count cities by region code
     */
    @Query("SELECT COUNT(c) FROM City c WHERE c.regionCode = :regionCode")
    long countByRegionCode(@Param("regionCode") String regionCode);
}
