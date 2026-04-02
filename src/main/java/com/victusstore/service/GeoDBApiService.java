package com.victusstore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victusstore.dto.GeoDBCityResponse;
import com.victusstore.dto.GeoDBRegionResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class GeoDBApiService {
    
    private static final String BASE_URL = "https://wft-geo-db.p.rapidapi.com";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    
    public GeoDBApiService(@Value("${geodb.api.key:}") String apiKey) {
        this.apiKey = apiKey;
        this.objectMapper = new ObjectMapper();
        
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }
    
    public List<GeoDBRegionResponse.RegionData> fetchRegions(String countryCode) throws IOException {
        String url = String.format("%s/v1/geo/countries/%s/regions", BASE_URL, countryCode);
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .addHeader("Content-Type", "application/json")
                .addHeader("x-rapidapi-host", "wft-geo-db.p.rapidapi.com")
                .addHeader("x-rapidapi-key", apiKey)
                .build();
        
        return executeWithRetry(request, GeoDBRegionResponse.class).getData();
    }
    
    public List<GeoDBCityResponse.CityData> fetchCitiesForRegion(String regionCode) throws IOException {
        List<GeoDBCityResponse.CityData> allCities = new ArrayList<>();
        int page = 0;
        int limit = 10;
        
        while (true) {
            String url = String.format("%s/v1/geo/regions/%s/cities?page=%d&limit=%d", 
                    BASE_URL, regionCode, page, limit);
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("x-rapidapi-host", "wft-geo-db.p.rapidapi.com")
                    .addHeader("x-rapidapi-key", apiKey)
                    .build();
            
            GeoDBCityResponse response = executeWithRetry(request, GeoDBCityResponse.class);
            
            if (response.getData() == null || response.getData().isEmpty()) {
                break;
            }
            
            allCities.addAll(response.getData());
            
            if (response.getData().size() < limit) {
                break;
            }
            
            page++;
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Thread interrupted during rate limiting", e);
            }
        }
        
        return allCities;
    }
    
    private <T> T executeWithRetry(Request request, Class<T> responseClass) throws IOException {
        IOException lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        if (response.code() == 429) {
                            log.warn("Rate limit hit, waiting before retry... Attempt {}/{}", attempt, MAX_RETRIES);
                            if (attempt < MAX_RETRIES) {
                                Thread.sleep(RETRY_DELAY_MS * attempt);
                                continue;
                            }
                        }
                        throw new IOException("HTTP " + response.code() + ": " + response.message());
                    }
                    
                    ResponseBody body = response.body();
                    if (body == null) {
                        throw new IOException("Empty response body");
                    }
                    
                    return objectMapper.readValue(body.string(), responseClass);
                }
            } catch (IOException e) {
                lastException = e;
                log.warn("Request failed on attempt {}/{}: {}", attempt, MAX_RETRIES, e.getMessage());
                
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Thread interrupted during retry delay", ie);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Thread interrupted during retry", e);
            }
        }
        
        throw new IOException("All retry attempts failed", lastException);
    }
}
