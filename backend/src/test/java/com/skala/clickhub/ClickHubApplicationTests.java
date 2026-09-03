package com.skala.clickhub;

import com.skala.clickhub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("nodb")
class ClickHubApplicationTests {

	// nodb 프로필은 DataSource/Hibernate를 배제해 실제 UserRepository 빈이 없다.
	// JwtAuthenticationFilter/AuthController가 이제 UserRepository를 필요로 하므로
	// 컨텍스트 로딩만 검증하는 이 테스트에서는 Mockito 목으로 대신 채운다.
	@MockitoBean
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

}
