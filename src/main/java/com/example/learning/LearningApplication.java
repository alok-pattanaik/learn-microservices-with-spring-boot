package com.example.learning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LESSON: @SpringBootApplication
 * This is a convenience annotation that combines three annotations:
 *   - @Configuration   → this class is a source of bean definitions
 *   - @EnableAutoConfiguration → Spring Boot auto-configures beans based on classpath
 *   - @ComponentScan   → scans this package and sub-packages for Spring components
 */
@SpringBootApplication
public class LearningApplication {
    public static void main(String[] args) {
        SpringApplication.run(LearningApplication.class, args);
    }
}
