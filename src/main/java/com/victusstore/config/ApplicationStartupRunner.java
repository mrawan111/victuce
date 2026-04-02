package com.victusstore.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.victusstore.service.EgyptGeoDataService;
import com.victusstore.service.GeoDBApiService;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationStartupRunner {
    
    private final EgyptGeoDataService egyptGeoDataService;
    private final GeoDBApiService geoDBApiService;
    
    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        log.info("=== APPLICATION STARTED SUCCESSFULLY ===");
        log.info("Available endpoints:");
        log.info("  POST http://localhost:8080/api/geo-data/import/egypt");
        log.info("  GET http://localhost:8080/api/geo-data/import/status");
        log.info("  GET http://localhost:8080/api/geo-data/test/us-regions");
        log.info("=====================================");
        
        // Auto-run test endpoint to verify API is working
        try {
            log.info("Auto-testing US regions API...");
            geoDBApiService.fetchRegions("US");
            log.info("✅ US regions API test successful!");
        } catch (Exception e) {
            log.error("❌ US regions API test failed: {}", e.getMessage());
        }
        
        // Auto-run Egypt import
        try {
            log.info("Auto-importing Egyptian geo data...");
            var result = egyptGeoDataService.importEgyptGeoData();
            log.info("✅ Egypt import completed: {} regions, {} cities", 
                     result.getTotalRegions(), result.getTotalCities());
        } catch (Exception e) {
            log.error("❌ Egypt import failed: {}", e.getMessage());
        }
        
        log.info("=== AUTO-EXECUTION COMPLETED ===");
    }
}
