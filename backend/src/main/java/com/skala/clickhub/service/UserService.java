package com.skala.clickhub.service;

import com.skala.clickhub.dto.auth.AuthDtos.MeResponse;
import com.skala.clickhub.dto.user.UserDtos.CreatorDetailResponse;
import com.skala.clickhub.dto.user.UserDtos.CreatorSummary;
import com.skala.clickhub.dto.user.UserDtos.OnboardingResponse;
import com.skala.clickhub.dto.user.UserDtos.OnboardingUpdateRequest;
import com.skala.clickhub.dto.user.UserDtos.ProfileUpdateRequest;
import com.skala.clickhub.dto.user.UserDtos.ProjectItem;
import com.skala.clickhub.entity.Category;
import com.skala.clickhub.entity.CreatorSubscription;
import com.skala.clickhub.entity.Project;
import com.skala.clickhub.entity.ProjectReaction;
import com.skala.clickhub.entity.ProjectStatus;
import com.skala.clickhub.entity.ReactionType;
import com.skala.clickhub.entity.Technology;
import com.skala.clickhub.entity.Theme;
import com.skala.clickhub.entity.User;
import com.skala.clickhub.entity.UserOnboardingInterestCategory;
import com.skala.clickhub.entity.UserOnboardingInterestTechnology;
import com.skala.clickhub.entity.UserOnboardingProfile;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.CategoryRepository;
import com.skala.clickhub.repository.CreatorSubscriptionRepository;
import com.skala.clickhub.repository.ProjectReactionRepository;
import com.skala.clickhub.repository.ProjectRepository;
import com.skala.clickhub.repository.TechnologyRepository;
import com.skala.clickhub.repository.UserOnboardingInterestCategoryRepository;
import com.skala.clickhub.repository.UserOnboardingInterestTechnologyRepository;
import com.skala.clickhub.repository.UserOnboardingProfileRepository;
import com.skala.clickhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserOnboardingProfileRepository onboardingProfileRepository;
    private final UserOnboardingInterestCategoryRepository onboardingCategoryRepository;
    private final UserOnboardingInterestTechnologyRepository onboardingTechnologyRepository;
    private final CategoryRepository categoryRepository;
    private final TechnologyRepository technologyRepository;
    private final ProjectRepository projectRepository;
    private final ProjectReactionRepository projectReactionRepository;
    private final CreatorSubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public MeResponse getMe(UUID userId) {
        return toMe(requireUser(userId));
    }

    @Transactional
    public MeResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        User user = requireUser(userId);
        String displayName = request.displayName();
        if (displayName != null) {
            displayName = displayName.trim();
            if (displayName.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }
        user.updateProfile(displayName, parseTheme(request.theme()), request.newProjectNotifications());
        return toMe(user);
    }

    @Transactional
    public OnboardingResponse updateOnboarding(UUID userId, OnboardingUpdateRequest request) {
        User user = requireUser(userId);
        List<String> goals = normalizedDistinct(request.goals());
        List<String> categorySlugs = normalizedDistinct(request.categorySlugs());
        List<String> technologySlugs = normalizedDistinct(request.technologySlugs());

        List<Category> categories = categorySlugs.stream()
                .map(slug -> categoryRepository.findBySlug(slug)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND)))
                .toList();
        List<Technology> technologies = technologySlugs.stream()
                .map(slug -> technologyRepository.findBySlug(slug)
                        .orElseThrow(() -> new BusinessException(ErrorCode.TECHNOLOGY_NOT_FOUND)))
                .toList();

        UserOnboardingProfile profile = onboardingProfileRepository.findById(userId)
                .orElseGet(() -> UserOnboardingProfile.builder()
                        .user(user)
                        .goals(goals.toArray(String[]::new))
                        .build());
        profile.updateGoals(goals.toArray(String[]::new));
        onboardingProfileRepository.save(profile);

        onboardingCategoryRepository.deleteByIdUserId(userId);
        onboardingCategoryRepository.flush();
        onboardingCategoryRepository.saveAll(categories.stream()
                .map(category -> UserOnboardingInterestCategory.builder()
                        .user(user)
                        .category(category)
                .build())
                .toList());
        onboardingCategoryRepository.flush();

        onboardingTechnologyRepository.deleteByIdUserId(userId);
        onboardingTechnologyRepository.flush();
        onboardingTechnologyRepository.saveAll(technologies.stream()
                .map(technology -> UserOnboardingInterestTechnology.builder()
                        .user(user)
                        .technology(technology)
                .build())
                .toList());
        onboardingTechnologyRepository.flush();

        return new OnboardingResponse(goals, categorySlugs, technologySlugs, profile.getCompletedAt());
    }

    @Transactional(readOnly = true)
    public List<ProjectItem> getMyProjects(UUID userId) {
        requireUser(userId);
        return projectRepository.findAllByOwnerId(userId).stream().map(this::toProjectItem).toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectItem> getMyFavorites(UUID userId) {
        requireUser(userId);
        return projectReactionRepository.findAllByUserAndType(userId, ReactionType.FAVORITE).stream()
                .map(ProjectReaction::getProject)
                .map(this::toProjectItem)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CreatorSummary> getMySubscriptions(UUID userId) {
        requireUser(userId);
        return subscriptionRepository.findAllBySubscriberId(userId).stream()
                .map(CreatorSubscription::getCreator)
                .map(this::toCreatorSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public CreatorDetailResponse getCreator(UUID creatorId, UUID viewerId) {
        User creator = requireUser(creatorId);
        List<ProjectItem> projects = projectRepository
                .findAllByOwnerIdAndStatus(creatorId, ProjectStatus.PUBLISHED)
                .stream()
                .map(this::toProjectItem)
                .toList();
        boolean subscribedByMe = viewerId != null
                && subscriptionRepository.existsById(new com.skala.clickhub.entity.id.CreatorSubscriptionId(
                        viewerId, creatorId));
        return new CreatorDetailResponse(
                creator.getId(),
                creator.getDisplayName(),
                creator.getAvatarUrl(),
                subscriptionRepository.countByIdCreatorId(creatorId),
                subscribedByMe,
                projects
        );
    }

    private MeResponse toMe(User user) {
        return new MeResponse(
                user.getId(),
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getRole().name(),
                user.getTheme().name(),
                user.getAuthProvider().name(),
                user.isNewProjectNotifications(),
                onboardingProfileRepository.existsById(user.getId())
        );
    }

    private ProjectItem toProjectItem(Project project) {
        return new ProjectItem(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getThumbnailUrl(),
                project.getPrimaryCategory() == null ? null : project.getPrimaryCategory().getSlug(),
                project.getPrimaryCategory() == null ? null : project.getPrimaryCategory().getName(),
                project.getStatus().name(),
                Arrays.asList(project.getTags()),
                project.getOwner().getDisplayName(),
                project.getOwner().getId(),
                project.getPublishedAt(),
                projectReactionRepository.countByIdProjectIdAndIdType(project.getId(), ReactionType.LIKE),
                projectReactionRepository.countByIdProjectIdAndIdType(project.getId(), ReactionType.FAVORITE)
        );
    }

    private CreatorSummary toCreatorSummary(User creator) {
        return new CreatorSummary(
                creator.getId(),
                creator.getDisplayName(),
                creator.getAvatarUrl(),
                subscriptionRepository.countByIdCreatorId(creator.getId()),
                projectRepository.findAllByOwnerIdAndStatus(creator.getId(), ProjectStatus.PUBLISHED).size()
        );
    }

    private User requireUser(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Theme parseTheme(String theme) {
        if (theme == null || theme.isBlank()) {
            return null;
        }
        try {
            return Theme.valueOf(theme.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private List<String> normalizedDistinct(List<String> values) {
        if (values == null) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String item = value == null ? "" : value.trim();
            if (item.isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            normalized.add(item);
        }
        return List.copyOf(normalized);
    }
}
