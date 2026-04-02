package com.victusstore.controller;

import com.victusstore.dto.AddCityRequest;
import com.victusstore.dto.CityDTO;
import com.victusstore.dto.RegionDTO;
import com.victusstore.model.City;
import com.victusstore.model.Region;
import com.victusstore.repository.CityRepository;
import com.victusstore.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Egypt Regions and Cities API
 * Provides endpoints for React frontend to consume
 */
@RestController
@RequestMapping("/api/egypt-regions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow all origins for development, adjust for production
public class EgyptRegionsController {

    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;

    /**
     * Get all regions
     * GET /api/egypt-regions/regions
     */
    @GetMapping("/regions")
    public ResponseEntity<List<RegionDTO>> getAllRegions() {
        log.info("Fetching all regions");
        try {
            List<Region> regions = regionRepository.findAllOrderByName();
            List<RegionDTO> regionDTOs = regions.stream()
                    .map(this::convertToRegionDTO)
                    .collect(Collectors.toList());
            
            log.info("Returned {} regions", regionDTOs.size());
            return ResponseEntity.ok(regionDTOs);
        } catch (Exception e) {
            log.error("Error fetching regions", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get region by code
     * GET /api/egypt-regions/regions/{regionCode}
     */
    @GetMapping("/regions/{regionCode}")
    public ResponseEntity<RegionDTO> getRegionByCode(@PathVariable String regionCode) {
        log.info("Fetching region with code: {}", regionCode);
        try {
            return regionRepository.findByRegionCode(regionCode)
                    .map(region -> {
                        RegionDTO dto = convertToRegionDTO(region);
                        log.info("Found region: {}", dto.getName());
                        return ResponseEntity.ok(dto);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching region with code: {}", regionCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get cities by region code
     * GET /api/egypt-regions/cities?regionCode={regionCode}
     */
    @GetMapping("/cities")
    public ResponseEntity<List<CityDTO>> getCitiesByRegion(@RequestParam String regionCode) {
        log.info("Fetching cities for region code: {}", regionCode);
        try {
            // First verify region exists
            if (!regionRepository.existsByRegionCode(regionCode)) {
                log.warn("Region not found: {}", regionCode);
                return ResponseEntity.notFound().build();
            }

            List<City> cities = cityRepository.findByRegionCodeOrderByName(regionCode);
            List<CityDTO> cityDTOs = cities.stream()
                    .map(this::convertToCityDTO)
                    .collect(Collectors.toList());
            
            log.info("Returned {} cities for region {}", cityDTOs.size(), regionCode);
            return ResponseEntity.ok(cityDTOs);
        } catch (Exception e) {
            log.error("Error fetching cities for region: {}", regionCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Add a new city
     * POST /api/egypt-regions/cities
     */
    @PostMapping("/cities")
    public ResponseEntity<CityDTO> addCity(@RequestBody AddCityRequest request) {
        log.info("Adding new city: {} for region: {}", request.getName(), request.getRegionCode());
        try {
            // Validate region exists
            if (!regionRepository.existsByRegionCode(request.getRegionCode())) {
                log.warn("Region not found: {}", request.getRegionCode());
                return ResponseEntity.badRequest().build();
            }

            // Check if city already exists
            if (cityRepository.existsByNameAndRegionCode(request.getName(), request.getRegionCode())) {
                log.warn("City already exists: {} in region {}", request.getName(), request.getRegionCode());
                return ResponseEntity.badRequest().build();
            }

            // Create and save new city
            City city = new City();
            city.setName(request.getName());
            city.setRegionCode(request.getRegionCode());
            city.setIsOther(false);

            City savedCity = cityRepository.save(city);
            CityDTO cityDTO = convertToCityDTO(savedCity);
            
            log.info("Successfully added city: {} with ID: {}", savedCity.getName(), savedCity.getId());
            return ResponseEntity.ok(cityDTO);
        } catch (Exception e) {
            log.error("Error adding city: {} for region: {}", request.getName(), request.getRegionCode(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get city by ID
     * GET /api/egypt-regions/cities/{cityId}
     */
    @GetMapping("/cities/{cityId}")
    public ResponseEntity<CityDTO> getCityById(@PathVariable Integer cityId) {
        log.info("Fetching city with ID: {}", cityId);
        try {
            return cityRepository.findById(cityId)
                    .map(city -> {
                        CityDTO dto = convertToCityDTO(city);
                        log.info("Found city: {}", dto.getName());
                        return ResponseEntity.ok(dto);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching city with ID: {}", cityId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get "Other" city for a region
     * GET /api/egypt-regions/cities/{regionCode}/other
     */
    @GetMapping("/cities/{regionCode}/other")
    public ResponseEntity<CityDTO> getOtherCityByRegion(@PathVariable String regionCode) {
        log.info("Fetching 'Other' city for region: {}", regionCode);
        try {
            return cityRepository.findOtherCityByRegionCode(regionCode)
                    .map(city -> {
                        CityDTO dto = convertToCityDTO(city);
                        log.info("Found 'Other' city: {}", dto.getName());
                        return ResponseEntity.ok(dto);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching 'Other' city for region: {}", regionCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get statistics about regions and cities
     * GET /api/egypt-regions/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        log.info("Fetching statistics");
        try {
            long regionCount = regionRepository.count();
            long cityCount = cityRepository.count();
            
            var stats = new Object() {
                public final long totalRegions = regionCount;
                public final long totalCities = cityCount;
                public final String message = "Egypt Regions and Cities Statistics";
            };
            
            log.info("Stats: {} regions, {} cities", regionCount, cityCount);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // Helper methods for converting entities to DTOs
    private RegionDTO convertToRegionDTO(Region region) {
        RegionDTO dto = new RegionDTO();
        dto.setId(region.getId());
        dto.setName(region.getName());
        dto.setRegionCode(region.getRegionCode());
        return dto;
    }

    private CityDTO convertToCityDTO(City city) {
        CityDTO dto = new CityDTO();
        dto.setId(city.getId());
        dto.setName(city.getName());
        dto.setRegionCode(city.getRegionCode());
        dto.setIsOther(city.getIsOther());
        return dto;
    }
}
