package com.moonrider.zamazon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(@NonNull CorsRegistry  registry) {
        registry.addMapping("/**") // Applies to all endpoints
                .allowedOrigins(
                        "http://localhost:3000", // Local frontend (React/Angular/etc)
                        "http://localhost:8080", // Local Spring Boot or Postman
                        "https://zamazon.com", // Production domain
                        "https://www.zamazon.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true); // Allow cookies, auth headers
    }
}
