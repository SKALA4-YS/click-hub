package com.skala.clickhub.dto.catalog;

import java.util.UUID;

public final class CatalogDtos {

    private CatalogDtos() {}

    public record CategoryResponse(UUID id, String name, String slug) {}

    public record TechnologyResponse(UUID id, String name, String slug, String defaultGroup) {}
}
