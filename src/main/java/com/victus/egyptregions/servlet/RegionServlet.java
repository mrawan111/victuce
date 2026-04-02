package com.victus.egyptregions.servlet;

import com.victus.egyptregions.dao.RegionDAO;
import com.victus.egyptregions.model.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/regions")
public class RegionServlet {

    private static final Logger logger = Logger.getLogger(RegionServlet.class.getName());

    private final RegionDAO regionDAO;

    @Autowired
    public RegionServlet(RegionDAO regionDAO) {
        this.regionDAO = regionDAO;
    }

    /** GET /api/regions — return all regions */
    @GetMapping
    public ResponseEntity<List<Region>> getAllRegions() {
        List<Region> regions = regionDAO.getAllRegions();
        logger.info("Returned " + regions.size() + " regions");
        return ResponseEntity.ok(regions);
    }

    /** GET /api/regions/{code} — return region by code */
    @GetMapping("/{code}")
    public ResponseEntity<?> getRegionByCode(@PathVariable("code") String regionCode) {
        Region region = regionDAO.getRegionByCode(regionCode);
        if (region == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"Region not found: " + regionCode + "\"}");
        }
        return ResponseEntity.ok(region);
    }

    /** POST /api/regions — create a new region */
    @PostMapping
    public ResponseEntity<?> createRegion(@RequestBody Region region) {
        if (region.getName() == null || region.getRegionCode() == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"Invalid region data\"}");
        }
        if (regionDAO.regionCodeExists(region.getRegionCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\": \"Region code already exists\"}");
        }
        if (regionDAO.insertRegion(region)) {
            logger.info("Created region: " + region);
            return ResponseEntity.status(HttpStatus.CREATED).body(region);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Failed to create region\"}");
    }
}
