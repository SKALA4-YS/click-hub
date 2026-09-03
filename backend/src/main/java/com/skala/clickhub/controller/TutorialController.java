package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.tutorial.TutorialDtos.TutorialResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TutorialController {

    // 기획서 9.2 — 카테고리·난이도·기술 스택·학습 시간으로 필터링. 인증: 선택
    @GetMapping("/v1/tutorials")
    public ApiResponse<List<TutorialResponse>> getTutorials(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String tech
    ) {
        throw new UnsupportedOperationException("not implemented");
    }
}
