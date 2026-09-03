package com.skala.clickhub;

import com.skala.clickhub.support.NoDbRepositoryMocks;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("nodb")
class ClickHubApplicationTests extends NoDbRepositoryMocks {

	@Test
	void contextLoads() {
	}

}
