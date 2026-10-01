package com.thanh.identityservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// Uses the in-memory H2 database from test.properties instead of MySQL
@SpringBootTest
@TestPropertySource("/test.properties")
class IdentityServiceApplicationTests {

    @Test
    void contextLoads() {}
}
