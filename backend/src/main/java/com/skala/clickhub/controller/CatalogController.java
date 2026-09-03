package com.skala.clickhub.controller;

import com.skala.clickhub.common.response.ApiResponse;
import com.skala.clickhub.dto.catalog.CatalogDtos.CategoryResponse;
import com.skala.clickhub.dto.catalog.CatalogDtos.TechnologyResponse;
import com.skala.clickhub.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        return ApiResponse.success(catalogService.getCategories());
    }

    @GetMapping("/technologies")
    public ApiResponse<List<TechnologyResponse>> getTechnologies() {
        return ApiResponse.success(catalogService.getTechnologies());
    }
}
