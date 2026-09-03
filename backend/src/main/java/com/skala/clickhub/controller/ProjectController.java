package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.project.ProjectDtos.CreateRequest;
import com.skala.clickhub.dto.project.ProjectDtos.CreateResponse;
import com.skala.clickhub.dto.project.ProjectDtos.DetailResponse;
import com.skala.clickhub.dto.project.ProjectDtos.OutboundClickResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/v1/projects")
public class ProjectController {

    // 인증: GitHub 로그인
    @PostMapping
    public ApiResponse<CreateResponse> createProject(@RequestBody CreateRequest request) {
        throw new UnsupportedOperationException("not implemented");
    }

    // 인증: 선택 - 로그인 시 likedByMe/favoritedByMe 개인화 필드 채움
    @GetMapping("/{id}")
    public ApiResponse<DetailResponse> getProjectDetail(@PathVariable UUID id) {
        throw new UnsupportedOperationException("not implemented");
    }

    // 인증: 선택
    @PostMapping("/{id}/outbound-clicks")
    public ApiResponse<OutboundClickResponse> recordOutboundClick(@PathVariable UUID id) {
        throw new UnsupportedOperationException("not implemented");
    }
}
