package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.ranking.RankingDtos.DeveloperRankingItem;
import com.skala.clickhub.dto.ranking.RankingDtos.ProjectRankingItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/rankings")
public class RankingController {

    // 기획서 7장 — 최근 7일 활동 기반 Top 100. 인증: 없음
    @GetMapping("/projects")
    public ApiResponse<List<ProjectRankingItem>> getProjectRanking() {
        throw new UnsupportedOperationException("not implemented");
    }

    // 기획서 9.3 — 개발자 랭킹. 인증: 없음
    @GetMapping("/developers")
    public ApiResponse<List<DeveloperRankingItem>> getDeveloperRanking() {
        throw new UnsupportedOperationException("not implemented");
    }
}
