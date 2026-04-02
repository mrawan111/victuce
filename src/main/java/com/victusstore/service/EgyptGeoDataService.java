package com.victusstore.service;

import com.victusstore.dto.GeoDBCityResponse;
import com.victusstore.dto.GeoDBRegionResponse;
import com.victusstore.model.City;
import com.victusstore.model.Region;
import com.victusstore.repository.CityRepository;
import com.victusstore.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EgyptGeoDataService {
    
    private static final String EGYPT_COUNTRY_CODE = "EG";
    
    private final GeoDBApiService geoDBApiService;
    private final RegionRepository regionRepository;
    private final CityRepository cityRepository;
    
    @Transactional
    public ImportResult importEgyptGeoData() throws IOException {
        log.info("Starting import of Egypt geo data...");
        
        List<Region> regions = fetchAndStoreRegions();
        List<City> cities = fetchAndStoreCities(regions);
        
        ImportResult result = new ImportResult();
        result.setTotalRegions(regions.size());
        result.setTotalCities(cities.size());
        
        log.info("Import completed. Regions: {}, Cities: {}", regions.size(), cities.size());
        return result;
    }
    
    private List<Region> fetchAndStoreRegions() throws IOException {
        log.info("Fetching regions for Egypt...");
        
        List<GeoDBRegionResponse.RegionData> apiRegions = geoDBApiService.fetchRegions(EGYPT_COUNTRY_CODE);
        log.info("Fetched {} regions from API", apiRegions.size());
        
        List<Region> regions = new ArrayList<>();
        for (GeoDBRegionResponse.RegionData apiRegion : apiRegions) {
            if (apiRegion.getIsoCode() == null || apiRegion.getIsoCode().trim().isEmpty()) {
                log.warn("Skipping region with empty ISO code: {}", apiRegion.getName());
                continue;
            }
            
            if (regionRepository.existsByRegionCode(apiRegion.getIsoCode())) {
                log.info("Region {} already exists, skipping", apiRegion.getIsoCode());
                continue;
            }
            
            Region region = new Region();
            region.setName(apiRegion.getName());
            region.setRegionCode(apiRegion.getIsoCode());
            
            regions.add(region);
            log.info("Added region: {} ({})", region.getName(), region.getRegionCode());
        }
        
        if (!regions.isEmpty()) {
            List<Region> savedRegions = regionRepository.saveAll(regions);
            log.info("Saved {} new regions to database", savedRegions.size());
        }
        
        return regionRepository.findAll();
    }
    
    private List<City> fetchAndStoreCities(List<Region> regions) throws IOException {
        log.info("Fetching cities for all regions...");
        
        List<City> allCities = new ArrayList<>();
        
        for (Region region : regions) {
            try {
                log.info("Fetching cities for region: {} ({})", region.getName(), region.getRegionCode());
                
                List<GeoDBCityResponse.CityData> apiCities = geoDBApiService.fetchCitiesForRegion(region.getRegionCode());
                log.info("Fetched {} cities for region {}", apiCities.size(), region.getRegionCode());
                
                List<City> citiesForRegion = new ArrayList<>();
                for (GeoDBCityResponse.CityData apiCity : apiCities) {
                    if (apiCity.getName() == null || apiCity.getName().trim().isEmpty()) {
                        log.warn("Skipping city with empty name in region {}", region.getRegionCode());
                        continue;
                    }
                    
                    City city = new City();
                    city.setName(apiCity.getName());
                    city.setRegionCode(region.getRegionCode());
                    
                    citiesForRegion.add(city);
                }
                
                if (!citiesForRegion.isEmpty()) {
                    cityRepository.deleteByRegionCode(region.getRegionCode());
                    List<City> savedCities = cityRepository.saveAll(citiesForRegion);
                    allCities.addAll(savedCities);
                    log.info("Saved {} cities for region {}", savedCities.size(), region.getRegionCode());
                }
                
            } catch (IOException e) {
                log.error("Failed to fetch cities for region {}: {}", region.getRegionCode(), e.getMessage());
                throw e;
            }
        }
        
        log.info("Total cities saved: {}", allCities.size());
        return allCities;
    }
    
    public static class ImportResult {
        private int totalRegions;
        private int totalCities;
        
        public int getTotalRegions() {
            return totalRegions;
        }
        
        public void setTotalRegions(int totalRegions) {
            this.totalRegions = totalRegions;
        }
        
        public int getTotalCities() {
            return totalCities;
        }
        
        public void setTotalCities(int totalCities) {
            this.totalCities = totalCities;
        }
        
        @Override
        public String toString() {
            return String.format("ImportResult{totalRegions=%d, totalCities=%d}", totalRegions, totalCities);
        }
    }
}
