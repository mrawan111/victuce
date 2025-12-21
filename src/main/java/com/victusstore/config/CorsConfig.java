package com.victusstore.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:8090}")
    private String allowedOrigins;
    
    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(origins.toArray(new String[0]))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .exposedHeaders("X-Trace-Id")
                        .allowCredentials(allowCredentials)
                        .maxAge(3600); // 1 hour
            }
        };
    }
}
