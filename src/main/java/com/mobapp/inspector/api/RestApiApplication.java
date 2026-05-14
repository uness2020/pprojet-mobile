package com.mobapp.inspector.api;

import com.mobapp.inspector.database.DatabaseService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot application for REST API endpoints.
 * This runs alongside the JavaFX UI.
 */
@SpringBootApplication
public class RestApiApplication {
    
    @Bean
    public DatabaseService databaseService() {
        return new DatabaseService();
    }
    
    public static void main(String[] args) {
        SpringApplication.run(RestApiApplication.class, args);
    }
}
