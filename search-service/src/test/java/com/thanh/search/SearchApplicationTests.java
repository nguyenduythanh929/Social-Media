package com.thanh.search;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties =
		"JWT_SIGNER_KEY=test-signer-key-test-signer-key-test-signer-key-test-signer-key-0123456789")
class SearchApplicationTests {

	@Test
	void contextLoads() {
	}

}
