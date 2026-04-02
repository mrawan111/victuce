package com.victusstore.controller;

import com.victusstore.dto.GeoDBRegionResponse;
import com.victusstore.service.EgyptGeoDataService;
import com.victusstore.service.GeoDBApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/geo-data")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Geo Data Import", description = "API for importing Egyptian regions and cities")
public class GeoDataController {
    
    private final EgyptGeoDataService egyptGeoDataService;
    private final GeoDBApiService geoDBApiService;
    
    @PostMapping("/import/egypt")
    @Operation(summary = "Import Egyptian regions and cities from GeoDB API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Import completed successfully"),
        @ApiResponse(responseCode = "500", description = "Import failed due to API or database error")
    })
    public ResponseEntity<?> importEgyptGeoData() {
        try {
            log.info("Received request to import Egyptian geo data");
            
            EgyptGeoDataService.ImportResult result = egyptGeoDataService.importEgyptGeoData();
            
            return ResponseEntity.ok(new ImportResponse(
                "Import completed successfully",
                result.getTotalRegions(),
                result.getTotalCities()
            ));
            
        } catch (IOException e) {
            log.error("Failed to import Egyptian geo data: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Import failed: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during import: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
    
    @GetMapping("/import/status")
    @Operation(summary = "Get import status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status retrieved successfully")
    })
    public ResponseEntity<?> getImportStatus() {
        return ResponseEntity.ok(new StatusResponse("Geo data import service is ready"));
    }
    
    private record ImportResponse(
        String message,
        int totalRegions,
        int totalCities
    ) {}
    
    private record StatusResponse(
        String status
    ) {}
    
    private record ErrorResponse(
        String error
    ) {}
    
    @GetMapping("/test/us-regions")
    @Operation(summary = "Test US regions API call")
    public ResponseEntity<?> testUSRegions() {
        try {
            log.info("Testing US regions API call...");
            
            List<GeoDBRegionResponse.RegionData> regions = geoDBApiService.fetchRegions("US");
            
            return ResponseEntity.ok(new TestResponse(
                "US regions fetched successfully",
                regions.size(),
                regions
            ));
            
        } catch (Exception e) {
            log.error("Failed to fetch US regions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(new ErrorResponse("Test failed: " + e.getMessage()));
        }
    }
    
    private record TestResponse(
        String message,
        int count,
        List<GeoDBRegionResponse.RegionData> data
    ) {}
}
