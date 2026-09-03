package com.skala.clickhub.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PingController {

	@GetMapping("/ping")
	public PingResponse ping() {
		return new PingResponse("ok", "click-hub-backend");
	}

	public record PingResponse(String status, String service) {
	}
}
