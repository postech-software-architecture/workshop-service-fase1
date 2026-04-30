package com.postech.workshop_service;

import com.postech.workshop_service.config.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WorkshopServiceApplicationIT extends PostgresTestContainer {

	@Test
	void contextLoads() {
	}

}
