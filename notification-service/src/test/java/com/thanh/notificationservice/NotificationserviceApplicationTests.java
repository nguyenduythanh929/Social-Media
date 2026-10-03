package com.thanh.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "SENDINBLUE_API_KEY=test-api-key")
class NotificationserviceApplicationTests {

    @Test
    void contextLoads() {}
}
