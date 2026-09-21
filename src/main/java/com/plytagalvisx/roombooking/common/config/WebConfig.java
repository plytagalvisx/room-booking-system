package com.plytagalvisx.roombooking.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("Authorization", "Content-Type");
    }
    // We're basically adding here port mapping for the frontend to be able to access the backend.
    // The frontend is running on port 5173 and the backend is running on port 8080.
    // So we need to allow the frontend and backend to communicate.

    // We're basically adding here CORS configuration to allow requests from the frontend
    // (which is running on http://localhost:5173) to our backend API endpoints.
}
