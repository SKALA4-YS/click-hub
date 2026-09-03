package com.skala.clickhub.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import com.skala.clickhub.repository.DeveloperRankingRepository;
import com.skala.clickhub.repository.ProjectRankingRepository;
import com.skala.clickhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "clickhub.cors.allowed-origins=https://click-hub.vercel.app")
@ActiveProfiles("nodb")
class PingControllerTests {

	private static final String ALLOWED_ORIGIN = "https://click-hub.vercel.app";

	// nodb 프로필엔 실제 UserRepository 빈이 없다 — JwtAuthenticationFilter가 이제 이걸 필요로
	// 하므로 컨텍스트 로딩을 위해 Mockito 목으로 채운다 (이 테스트는 인증 로직을 검증하지 않음).
	@MockitoBean
	private UserRepository userRepository;

	// RankingService가 필요로 하는 뷰 전용 리포지토리도 같은 이유로 목으로 채운다.
	@MockitoBean
	private ProjectRankingRepository projectRankingRepository;

	@MockitoBean
	private DeveloperRankingRepository developerRankingRepository;

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
	}

	@Test
	void returnsPingContract() throws Exception {
		mockMvc.perform(get("/api/v1/ping"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ok"))
				.andExpect(jsonPath("$.service").value("click-hub-backend"));
	}

	@Test
	void allowsConfiguredOrigin() throws Exception {
		mockMvc.perform(get("/api/v1/ping").header(HttpHeaders.ORIGIN, ALLOWED_ORIGIN))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN));
	}

	@Test
	void doesNotAllowUnconfiguredOrigin() throws Exception {
		mockMvc.perform(get("/api/v1/ping").header(HttpHeaders.ORIGIN, "https://example.com"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
	}
}
