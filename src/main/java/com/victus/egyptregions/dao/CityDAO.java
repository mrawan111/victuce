package com.victus.egyptregions.dao;

import com.victus.egyptregions.model.City;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for City operations
 * Handles all database operations related to cities
 */
@Repository
public class CityDAO {
    private static final Logger logger = Logger.getLogger(CityDAO.class.getName());

    private final DataSource dataSource;

    @Autowired
    public CityDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    // SQL queries
    private static final String GET_CITIES_BY_REGION = 
        "SELECT id, name, region_code, is_other FROM cities WHERE region_code = ? ORDER BY is_other ASC, name ASC";
    private static final String GET_CITY_BY_ID = 
        "SELECT id, name, region_code, is_other FROM cities WHERE id = ?";
    private static final String INSERT_CITY = 
        "INSERT INTO cities (name, region_code, is_other) VALUES (?, ?, ?)";
    private static final String UPDATE_CITY = 
        "UPDATE cities SET name = ?, region_code = ?, is_other = ? WHERE id = ?";
    private static final String DELETE_CITY = 
        "DELETE FROM cities WHERE id = ?";
    private static final String GET_OTHER_CITY_BY_REGION = 
        "SELECT id, name, region_code, is_other FROM cities WHERE region_code = ? AND is_other = TRUE";
    private static final String CHECK_CITY_EXISTS = 
        "SELECT COUNT(*) FROM cities WHERE name = ? AND region_code = ?";
    
    /**
     * Get all cities for a specific region, sorted by name (normal cities first, then Other)
     * @param regionCode The region code to get cities for
     * @return List of cities for the region
     */
    public List<City> getCitiesByRegion(String regionCode) {
        List<City> cities = new ArrayList<>();
        String sql = GET_CITIES_BY_REGION;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, regionCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    City city = mapResultSetToCity(rs);
                    cities.add(city);
                }
            }
            
            logger.info("Retrieved " + cities.size() + " cities for region: " + regionCode);
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving cities for region: " + regionCode, ex);
        }
        
        return cities;
    }
    
    /**
     * Get city by ID
     * @param id The city ID to search for
     * @return City object if found, null otherwise
     */
    public City getCityById(int id) {
        String sql = GET_CITY_BY_ID;
        City city = null;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    city = mapResultSetToCity(rs);
                    logger.info("Found city: " + city);
                }
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving city by ID: " + id, ex);
        }
        
        return city;
    }
    
    /**
     * Insert a new city
     * @param city The city to insert
     * @return true if insertion successful, false otherwise
     */
    public boolean insertCity(City city) {
        // Check if city already exists in the region
        if (cityExists(city.getName(), city.getRegionCode())) {
            logger.warning("City already exists in region: " + city.getName() + " in " + city.getRegionCode());
            return false;
        }
        
        String sql = INSERT_CITY;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, city.getName());
            stmt.setString(2, city.getRegionCode());
            stmt.setBoolean(3, city.isOther());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Successfully inserted city: " + city);
                return true;
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error inserting city: " + city, ex);
        }
        
        return false;
    }
    
    /**
     * Update an existing city
     * @param city The city to update
     * @return true if update successful, false otherwise
     */
    public boolean updateCity(City city) {
        String sql = UPDATE_CITY;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, city.getName());
            stmt.setString(2, city.getRegionCode());
            stmt.setBoolean(3, city.isOther());
            stmt.setInt(4, city.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Successfully updated city: " + city);
                return true;
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating city: " + city, ex);
        }
        
        return false;
    }
    
    /**
     * Delete a city by ID
     * @param id The city ID to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteCity(int id) {
        String sql = DELETE_CITY;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Successfully deleted city with ID: " + id);
                return true;
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error deleting city with ID: " + id, ex);
        }
        
        return false;
    }
    
    /**
     * Get the "Other" city for a region
     * @param regionCode The region code
     * @return Other city if exists, null otherwise
     */
    public City getOtherCityByRegion(String regionCode) {
        String sql = GET_OTHER_CITY_BY_REGION;
        City city = null;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, regionCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    city = mapResultSetToCity(rs);
                }
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving Other city for region: " + regionCode, ex);
        }
        
        return city;
    }
    
    /**
     * Check if a city already exists in a region
     * @param cityName The city name to check
     * @param regionCode The region code
     * @return true if city exists, false otherwise
     */
    public boolean cityExists(String cityName, String regionCode) {
        String sql = CHECK_CITY_EXISTS;
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cityName);
            stmt.setString(2, regionCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error checking if city exists: " + cityName + " in " + regionCode, ex);
        }
        
        return false;
    }
    
    /**
     * Add a custom city (when user selects "Other")
     * @param cityName The custom city name
     * @param regionCode The region code
     * @return true if addition successful, false otherwise
     */
    public boolean addCustomCity(String cityName, String regionCode) {
        if (cityName == null || cityName.trim().isEmpty()) {
            logger.warning("City name cannot be empty");
            return false;
        }
        
        // Check if city already exists
        if (cityExists(cityName.trim(), regionCode)) {
            logger.warning("City already exists: " + cityName + " in " + regionCode);
            return false;
        }
        
        City newCity = new City();
        newCity.setName(cityName.trim());
        newCity.setRegionCode(regionCode);
        newCity.setOther(false); // Custom cities are not "Other" type
        
        return insertCity(newCity);
    }
    
    /**
     * Map ResultSet to City object
     * @param rs The ResultSet to map
     * @return City object
     * @throws SQLException if there's an error accessing the ResultSet
     */
    private City mapResultSetToCity(ResultSet rs) throws SQLException {
        City city = new City();
        city.setId(rs.getInt("id"));
        city.setName(rs.getString("name"));
        city.setRegionCode(rs.getString("region_code"));
        city.setOther(rs.getBoolean("is_other"));
        return city;
    }
}
