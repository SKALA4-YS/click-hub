package com.skala.clickhub.service;

import com.skala.clickhub.dto.catalog.CatalogDtos.CategoryResponse;
import com.skala.clickhub.dto.catalog.CatalogDtos.TechnologyResponse;
import com.skala.clickhub.repository.CategoryRepository;
import com.skala.clickhub.repository.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CategoryRepository categoryRepository;
    private final TechnologyRepository technologyRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName(), category.getSlug()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TechnologyResponse> getTechnologies() {
        return technologyRepository.findAllByOrderByNameAsc().stream()
                .map(technology -> new TechnologyResponse(
                        technology.getId(),
                        technology.getName(),
                        technology.getSlug(),
                        technology.getDefaultGroup().name()))
                .toList();
    }
}
