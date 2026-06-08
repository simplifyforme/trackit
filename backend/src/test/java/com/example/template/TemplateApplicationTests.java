package com.example.template;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Full context load test.
 *
 * Disabled by default because it requires a running PostgreSQL instance.
 * To run:
 *   1. docker-compose up -d
 *   2. mvn test -Dspring.profiles.active=dev
 *
 * For CI, add Testcontainers (org.testcontainers:postgresql) and remove @Disabled.
 */
@SpringBootTest
@ActiveProfiles("dev")
@Disabled("Requires PostgreSQL — start docker-compose first, then remove @Disabled")
class TemplateApplicationTests {

    @Test
    void contextLoads() {
    }
}
