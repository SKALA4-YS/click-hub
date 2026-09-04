package com.skala.clickhub.service;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 프로젝트에 별도 썸네일이 없을 때 사용하는 카테고리별 발표/서비스 기본 이미지.
 *
 * <p>Render 컨테이너 파일시스템에 이미지를 저장하지 않고 브라우저가 공개 이미지 URL을
 * 직접 읽도록 한다. 새 카테고리가 추가되더라도 OTHER_URL로 안전하게 대체한다.</p>
 */
@Component
public class CategoryThumbnailProvider {

    private static final String IMAGE_OPTIONS = "?auto=format&fit=crop&w=1200&q=80";
    private static final String OTHER_URL =
            "https://images.unsplash.com/photo-1516321318423-f06f85e504b3" + IMAGE_OPTIONS;

    private static final Map<String, String> URLS = Map.ofEntries(
            Map.entry("productivity-work", image("photo-1497215728101-856f4ea42174")),
            Map.entry("education-career", image("photo-1523240795612-9a054b0db644")),
            Map.entry("developer-tools", image("photo-1498050108023-c5249f4df085")),
            Map.entry("finance", image("photo-1579621970563-ebec7560ff3e")),
            Map.entry("life-health", image("photo-1506126613408-eca07ce68773")),
            Map.entry("content-entertainment", image("photo-1489599849927-2ee91cede3ba")),
            Map.entry("social-community", image("photo-1529156069898-49953e39b3ac")),
            Map.entry("shopping-commerce", image("photo-1472851294608-062f824d29cc")),
            Map.entry("travel-local", image("photo-1500530855697-b586d89ba3ee")),
            Map.entry("design-creative", image("photo-1545235617-9465d2a55698")),
            Map.entry("ai-service", image("photo-1677442136019-21780ecad995")),
            Map.entry("data-analytics", image("photo-1551288049-bebda4e38f71")),
            Map.entry("security-auth", image("photo-1563013544-824ae1b704d3")),
            Map.entry("other", OTHER_URL)
    );

    public String resolve(String requestedThumbnailUrl, String categorySlug) {
        if (requestedThumbnailUrl != null && !requestedThumbnailUrl.isBlank()) {
            return requestedThumbnailUrl.trim();
        }
        return categorySlug == null ? OTHER_URL : URLS.getOrDefault(categorySlug, OTHER_URL);
    }

    private static String image(String photoId) {
        return "https://images.unsplash.com/" + photoId + IMAGE_OPTIONS;
    }
}
