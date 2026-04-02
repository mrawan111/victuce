package com.victus.egyptregions.servlet;

import com.victus.egyptregions.dao.CityDAO;
import com.victus.egyptregions.model.City;
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
 * Servlet for handling city-related requests
 * Provides REST API endpoints for city operations
 */
@WebServlet(name = "CityServlet", urlPatterns = {"/api/cities", "/api/cities/*"})
public class CityServlet extends HttpServlet {
    private static final Logger logger = Logger.getLogger(CityServlet.class.getName());
    private CityDAO cityDAO;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        cityDAO = new CityDAO();
        gson = new Gson();
        logger.info("CityServlet initialized");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String regionCode = request.getParameter("regionCode");
        logger.info("GET request to CityServlet with path: " + pathInfo + ", regionCode: " + regionCode);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/cities?regionCode={code} - Get cities by region code
                if (regionCode != null && !regionCode.isEmpty()) {
                    handleGetCitiesByRegion(regionCode, response);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                                     "regionCode parameter is required");
                }
            } else {
                // GET /api/cities/{id} - Get city by ID
                try {
                    int cityId = Integer.parseInt(pathInfo.substring(1)); // Remove leading slash
                    handleGetCityById(cityId, response);
                } catch (NumberFormatException e) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                                     "Invalid city ID format");
                }
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
        
        logger.info("POST request to CityServlet");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Parse city data from request
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            City city = gson.fromJson(sb.toString(), City.class);
            
            if (city == null || city.getName() == null || city.getRegionCode() == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                                 "Invalid city data");
                return;
            }
            
            // Insert new city
            if (cityDAO.insertCity(city)) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                sendJsonResponse(response, gson.toJson(city));
                logger.info("Successfully created city: " + city);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                 "Failed to create city or city already exists");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling POST request", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                             "Internal server error");
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        logger.info("PUT request to CityServlet");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Parse city data from request
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = request.getReader().readLine()) != null) {
                sb.append(line);
            }
            
            City city = gson.fromJson(sb.toString(), City.class);
            
            if (city == null || city.getId() <= 0 || city.getName() == null || city.getRegionCode() == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                                 "Invalid city data or missing ID");
                return;
            }
            
            // Update existing city
            if (cityDAO.updateCity(city)) {
                sendJsonResponse(response, gson.toJson(city));
                logger.info("Successfully updated city: " + city);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                 "Failed to update city");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling PUT request", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                             "Internal server error");
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        logger.info("DELETE request to CityServlet with path: " + pathInfo);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                                 "City ID is required for deletion");
                return;
            }
            
            try {
                int cityId = Integer.parseInt(pathInfo.substring(1)); // Remove leading slash
                
                if (cityDAO.deleteCity(cityId)) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    logger.info("Successfully deleted city with ID: " + cityId);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                                     "City not found or failed to delete");
                }
                
            } catch (NumberFormatException e) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                                 "Invalid city ID format");
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error handling DELETE request", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                             "Internal server error");
        }
    }
    
    /**
     * Handle GET /api/cities?regionCode={code} - Return cities by region
     */
    private void handleGetCitiesByRegion(String regionCode, HttpServletResponse response) 
            throws IOException {
        
        List<City> cities = cityDAO.getCitiesByRegion(regionCode);
        
        if (cities.isEmpty()) {
            logger.warning("No cities found for region: " + regionCode);
        }
        
        String jsonResponse = gson.toJson(cities);
        sendJsonResponse(response, jsonResponse);
        
        logger.info("Returned " + cities.size() + " cities for region: " + regionCode);
    }
    
    /**
     * Handle GET /api/cities/{id} - Return city by ID
     */
    private void handleGetCityById(int cityId, HttpServletResponse response) 
            throws IOException {
        
        City city = cityDAO.getCityById(cityId);
        
        if (city == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                             "City not found: " + cityId);
            return;
        }
        
        String jsonResponse = gson.toJson(city);
        sendJsonResponse(response, jsonResponse);
        
        logger.info("Returned city: " + city);
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
        logger.info("CityServlet destroyed");
    }
}
