package com.skala.clickhub.service;

import com.skala.clickhub.dto.tutorial.TutorialDtos.TutorialResponse;
import com.skala.clickhub.entity.Tutorial;
import com.skala.clickhub.entity.TutorialDifficulty;
import com.skala.clickhub.entity.TutorialType;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.TutorialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 튜토리얼 목록/필터 (기획서 9.2). */
@Service
@RequiredArgsConstructor
public class TutorialService {

    private final TutorialRepository tutorialRepository;

    @Transactional(readOnly = true)
    public List<TutorialResponse> getTutorials(String type, String difficulty, String tech) {
        // 잘못된 enum 값이 그대로 SQL 캐스팅까지 내려가면 DB 오류가 나므로 미리 검증한다.
        validateEnum(type, TutorialType.class);
        validateEnum(difficulty, TutorialDifficulty.class);

        return tutorialRepository.findPublished(
                        normalize(type),
                        normalize(difficulty),
                        tech == null ? "" : tech.trim())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private <E extends Enum<E>> void validateEnum(String value, Class<E> enumType) {
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? "" : value.trim().toUpperCase();
    }

    private TutorialResponse toResponse(Tutorial tutorial) {
        return new TutorialResponse(
                tutorial.getId(),
                tutorial.getTitle(),
                tutorial.getDescription(),
                tutorial.getType().name(),
                tutorial.getDifficulty().name(),
                tutorial.getEstimatedMinutes(),
                tutorial.getSourceUrl(),
                List.of(tutorial.getCategorySlugs()),
                List.of(tutorial.getTechnologySlugs())
        );
    }
}
