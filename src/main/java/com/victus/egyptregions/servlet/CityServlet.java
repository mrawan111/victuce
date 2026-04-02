package com.victus.egyptregions.servlet;

import com.victus.egyptregions.dao.CityDAO;
import com.victus.egyptregions.model.City;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/cities")
public class CityServlet {

    private static final Logger logger = Logger.getLogger(CityServlet.class.getName());

    private final CityDAO cityDAO;

    @Autowired
    public CityServlet(CityDAO cityDAO) {
        this.cityDAO = cityDAO;
    }

    /** GET /api/cities?regionCode={code} — return cities for a region */
    @GetMapping
    public ResponseEntity<?> getCitiesByRegion(@RequestParam(required = false) String regionCode) {
        if (regionCode == null || regionCode.isEmpty()) {
            return ResponseEntity.badRequest().body("{\"error\": \"regionCode parameter is required\"}");
        }
        List<City> cities = cityDAO.getCitiesByRegion(regionCode);
        logger.info("Returned " + cities.size() + " cities for region: " + regionCode);
        return ResponseEntity.ok(cities);
    }

    /** GET /api/cities/{id} — return city by ID */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCityById(@PathVariable int id) {
        City city = cityDAO.getCityById(id);
        if (city == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\": \"City not found: " + id + "\"}");
        }
        return ResponseEntity.ok(city);
    }

    /** POST /api/cities — create a new city */
    @PostMapping
    public ResponseEntity<?> createCity(@RequestBody City city) {
        if (city.getName() == null || city.getRegionCode() == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"Invalid city data\"}");
        }
        if (cityDAO.insertCity(city)) {
            logger.info("Created city: " + city);
            return ResponseEntity.status(HttpStatus.CREATED).body(city);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Failed to create city or city already exists\"}");
    }

    /** PUT /api/cities — update an existing city */
    @PutMapping
    public ResponseEntity<?> updateCity(@RequestBody City city) {
        if (city.getId() <= 0 || city.getName() == null || city.getRegionCode() == null) {
            return ResponseEntity.badRequest().body("{\"error\": \"Invalid city data or missing ID\"}");
        }
        if (cityDAO.updateCity(city)) {
            logger.info("Updated city: " + city);
            return ResponseEntity.ok(city);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("{\"error\": \"Failed to update city\"}");
    }

    /** DELETE /api/cities/{id} — delete a city by ID */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCity(@PathVariable int id) {
        if (cityDAO.deleteCity(id)) {
            logger.info("Deleted city with ID: " + id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\": \"City not found or failed to delete\"}");
    }
}
