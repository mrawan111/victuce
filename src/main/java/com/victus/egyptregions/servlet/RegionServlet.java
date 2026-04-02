package com.victus.egyptregions.servlet;

import com.victus.egyptregions.dao.RegionDAO;
import com.victus.egyptregions.model.Region;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet for handling region-related requests
 * Provides REST API endpoints for region operations
 */
@WebServlet(name = "RegionServlet", urlPatterns = {"/api/regions", "/api/regions/*"})
public class RegionServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(RegionServlet.class.getName());
    private RegionDAO regionDAO;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        regionDAO = new RegionDAO();
        gson = new Gson();
        logger.info("RegionServlet initialized");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        logger.info("GET request to RegionServlet with path: " + pathInfo);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/regions - Get all regions
                handleGetAllRegions(response);
            } else {
                // GET /api/regions/{code} - Get region by code
                String regionCode = pathInfo.substring(1); // Remove leading slash
                handleGetRegionByCode(regionCode, response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling GET request", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                             "Internal server error");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        logger.info("POST request to RegionServlet");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Parse region data from request
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            Region region = gson.fromJson(sb.toString(), Region.class);
            
            if (region == null || region.getName() == null || region.getRegionCode() == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                                 "Invalid region data");
                return;
            }
            
            // Check if region code already exists
            if (regionDAO.regionCodeExists(region.getRegionCode())) {
                sendErrorResponse(response, HttpServletResponse.SC_CONFLICT, 
                                 "Region code already exists");
                return;
            }
            
            // Insert new region
            if (regionDAO.insertRegion(region)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                sendJsonResponse(response, gson.toJson(region));
                logger.info("Successfully created region: " + region);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                 "Failed to create region");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling POST request", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                             "Internal server error");
        }
    }
    
    /**
     * Handle GET /api/regions - Return all regions
     */
    private void handleGetAllRegions(HttpServletResponse response) throws IOException {
        List<Region> regions = regionDAO.getAllRegions();
        
        if (regions.isEmpty()) {
            logger.warning("No regions found in database");
        }
        
        String jsonResponse = gson.toJson(regions);
        sendJsonResponse(response, jsonResponse);
        
        logger.info("Returned " + regions.size() + " regions");
    }
    
    /**
     * Handle GET /api/regions/{code} - Return region by code
     */
    private void handleGetRegionByCode(String regionCode, HttpServletResponse response) 
            throws IOException {
        
        Region region = regionDAO.getRegionByCode(regionCode);
        
        if (region == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                             "Region not found: " + regionCode);
            return;
        }
        
        String jsonResponse = gson.toJson(region);
        sendJsonResponse(response, jsonResponse);
        
        logger.info("Returned region: " + region);
    }
    
    /**
     * Send JSON response
     */
    private void sendJsonResponse(HttpServletResponse response, String jsonResponse) 
            throws IOException {
        PrintWriter out = response.getWriter();
        out.print(jsonResponse);
        out.flush();
    }
    
    /**
     * Send error response
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) 
            throws IOException {
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.print("{\"error\": \"" + message + "\"}");
        out.flush();
    }
    
    @Override
    public void destroy() {
        super.destroy();
        logger.info("RegionServlet destroyed");
    }
}
