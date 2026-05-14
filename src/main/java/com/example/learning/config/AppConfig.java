package com.example.learning.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * LESSON: @Configuration
 * Marks this class as a source of bean definitions.
 *
 * Two important guarantees @Configuration provides:
 *   1. Spring uses CGLIB to subclass this class at runtime.
 *      When one @Bean method calls another, Spring intercepts the call
 *      and returns the EXISTING singleton — not a new instance.
 *   2. All beans defined here are singletons by default.
 *
 * Without @Configuration (if you used plain @Component instead):
 *   clockA() calling clock() would create a NEW Clock instance each time,
 *   breaking the singleton contract and causing subtle bugs.
 *
 * -----------------------------------------------------------------------
 * LESSON: @Bean vs @Component
 *
 * @Component   → class-level; Spring detects and registers it automatically
 *                via component scanning; used for YOUR own classes.
 *
 * @Bean        → method-level; you write the construction logic yourself;
 *                preferred for THIRD-PARTY classes you cannot annotate
 *                (e.g. Jackson ObjectMapper, Clock, RestTemplate).
 *
 * Rule of thumb:
 *   Own class you can edit  →  @Component / @Service / @Repository
 *   Third-party class       →  @Bean inside a @Configuration class
 */
@Configuration
public class AppConfig {

    /**
     * Registering a java.time.Clock bean.
     * We cannot add @Component to Clock (it is a JDK class), so @Bean
     * is the correct approach. Any service that needs the clock just
     * declares Clock as a constructor parameter and Spring injects this.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
