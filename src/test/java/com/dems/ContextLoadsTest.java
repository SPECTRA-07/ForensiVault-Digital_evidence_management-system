package com.dems;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to verify that the Spring ApplicationContext loads cleanly.
 */
@SpringBootTest
@ActiveProfiles("test")
class ContextLoadsTest {

    @Test
    void contextLoads() {
        // Test passes if Spring application context initializes without errors
    }
}
