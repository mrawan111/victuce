package com.victus.egyptregions.dao;

import com.victus.egyptregions.model.Region;
import com.victus.egyptregions.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Region operations
 * Handles all database operations related to regions
 */
public class RegionDAO {
    private static final Logger logger = Logger.getLogger(RegionDAO.class.getName());
    
    // SQL queries
    private static final String GET_ALL_REGIONS = "SELECT id, name, region_code FROM regions ORDER BY name";
    private static final String GET_REGION_BY_CODE = "SELECT id, name, region_code FROM regions WHERE region_code = ?";
    private static final String GET_REGION_BY_ID = "SELECT id, name, region_code FROM regions WHERE id = ?";
    private static final String INSERT_REGION = "INSERT INTO regions (name, region_code) VALUES (?, ?)";
    private static final String UPDATE_REGION = "UPDATE regions SET name = ?, region_code = ? WHERE id = ?";
    private static final String DELETE_REGION = "DELETE FROM regions WHERE id = ?";
    
    /**
     * Get all regions from database
     * @return List of all regions
     */
    public List<Region> getAllRegions() {
        List<Region> regions = new ArrayList<>();
        String sql = GET_ALL_REGIONS;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Region region = mapResultSetToRegion(rs);
                regions.add(region);
            }
            
            logger.info("Retrieved " + regions.size() + " regions from database");
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving all regions", ex);
        }
        
        return regions;
    }
    
    /**
     * Get region by region code
     * @param regionCode The region code to search for
     * @return Region object if found, null otherwise
     */
    public Region getRegionByCode(String regionCode) {
        String sql = GET_REGION_BY_CODE;
        Region region = null;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, regionCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    region = mapResultSetToRegion(rs);
                    logger.info("Found region: " + region);
                }
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving region by code: " + regionCode, ex);
        }
        
        return region;
    }
    
    /**
     * Get region by ID
     * @param id The region ID to search for
     * @return Region object if found, null otherwise
     */
    public Region getRegionById(int id) {
        String sql = GET_REGION_BY_ID;
        Region region = null;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    region = mapResultSetToRegion(rs);
                    logger.info("Found region: " + region);
                }
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error retrieving region by ID: " + id, ex);
        }
        
        return region;
    }
    
    /**
     * Insert a new region
     * @param region The region to insert
     * @return true if insertion successful, false otherwise
     */
    public boolean insertRegion(Region region) {
        String sql = INSERT_REGION;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, region.getName());
            stmt.setString(2, region.getRegionCode());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Successfully inserted region: " + region);
                return true;
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error inserting region: " + region, ex);
        }
        
        return false;
    }
    
    /**
     * Update an existing region
     * @param region The region to update
     * @return true if update successful, false otherwise
     */
    public boolean updateRegion(Region region) {
        String sql = UPDATE_REGION;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, region.getName());
            stmt.setString(2, region.getRegionCode());
            stmt.setInt(3, region.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Successfully updated region: " + region);
                return true;
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating region: " + region, ex);
        }
        
        return false;
    }
    
    /**
     * Delete a region by ID
     * @param id The region ID to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteRegion(int id) {
        String sql = DELETE_REGION;
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Successfully deleted region with ID: " + id);
                return true;
            }
            
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error deleting region with ID: " + id, ex);
        }
        
        return false;
    }
    
    /**
     * Check if a region code already exists
     * @param regionCode The region code to check
     * @return true if region code exists, false otherwise
     */
    public boolean regionCodeExists(String regionCode) {
        return getRegionByCode(regionCode) != null;
    }
    
    /**
     * Map ResultSet to Region object
     * @param rs The ResultSet to map
     * @return Region object
     * @throws SQLException if there's an error accessing the ResultSet
     */
    private Region mapResultSetToRegion(ResultSet rs) throws SQLException {
        Region region = new Region();
        region.setId(rs.getInt("id"));
        region.setName(rs.getString("name"));
        region.setRegionCode(rs.getString("region_code"));
        return region;
    }
}
