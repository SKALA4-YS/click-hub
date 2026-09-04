package com.skala.clickhub.support;

import com.skala.clickhub.repository.AnonymousSessionRepository;
import com.skala.clickhub.repository.CategoryRepository;
import com.skala.clickhub.repository.CommunityBoardRepository;
import com.skala.clickhub.repository.CommunityPostCommentRepository;
import com.skala.clickhub.repository.CommunityPostRepository;
import com.skala.clickhub.repository.CreatorSubscriptionRepository;
import com.skala.clickhub.repository.DashboardAiAnalysisRepository;
import com.skala.clickhub.repository.InteractionEventRepository;
import com.skala.clickhub.repository.DeveloperRankingRepository;
import com.skala.clickhub.repository.NotificationRepository;
import com.skala.clickhub.repository.ProjectCommentRepository;
import com.skala.clickhub.repository.ProjectDailyMetricRepository;
import com.skala.clickhub.repository.ProjectRankingRepository;
import com.skala.clickhub.repository.ProjectReactionRepository;
import com.skala.clickhub.repository.ProjectRepository;
import com.skala.clickhub.repository.ProjectTechnologyRepository;
import com.skala.clickhub.repository.TechnologyRepository;
import com.skala.clickhub.repository.TutorialRepository;
import com.skala.clickhub.repository.UserOnboardingInterestCategoryRepository;
import com.skala.clickhub.repository.UserOnboardingInterestTechnologyRepository;
import com.skala.clickhub.repository.UserOnboardingProfileRepository;
import com.skala.clickhub.repository.UserRepository;
import com.skala.clickhub.repository.WeeklyInsightRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * "nodb" 프로필 테스트용 리포지토리 목 모음.
 *
 * nodb는 DataSource/Hibernate 자동 구성을 배제하므로 Spring Data JPA 리포지토리 빈이 아예 만들어지지
 * 않는다. 그런데 서비스 계층은 컴포넌트 스캔으로 그대로 등록되기 때문에, 목을 채워주지 않으면
 * 컨텍스트 로딩 자체가 실패한다(= 컨텍스트 로딩/CORS만 검증하는 테스트가 도메인 코드 추가마다 깨진다).
 *
 * 새 리포지토리를 추가하면 여기에도 한 줄 추가해야 한다. JdbcTemplate도 DataSource가 있어야
 * 자동 구성되므로(ProjectService.approve()가 record_project_url_validation() 네이티브 호출에 씀)
 * 같은 이유로 목이 필요하다.
 */
public abstract class NoDbRepositoryMocks {

    @MockitoBean
    protected JdbcTemplate jdbcTemplate;

    @MockitoBean
    protected UserRepository userRepository;

    @MockitoBean
    protected UserOnboardingProfileRepository userOnboardingProfileRepository;

    @MockitoBean
    protected UserOnboardingInterestCategoryRepository userOnboardingInterestCategoryRepository;

    @MockitoBean
    protected UserOnboardingInterestTechnologyRepository userOnboardingInterestTechnologyRepository;

    @MockitoBean
    protected ProjectRepository projectRepository;

    @MockitoBean
    protected ProjectTechnologyRepository projectTechnologyRepository;

    @MockitoBean
    protected ProjectReactionRepository projectReactionRepository;

    @MockitoBean
    protected ProjectCommentRepository projectCommentRepository;

    @MockitoBean
    protected CategoryRepository categoryRepository;

    @MockitoBean
    protected TechnologyRepository technologyRepository;

    @MockitoBean
    protected InteractionEventRepository interactionEventRepository;

    @MockitoBean
    protected AnonymousSessionRepository anonymousSessionRepository;

    @MockitoBean
    protected CreatorSubscriptionRepository creatorSubscriptionRepository;

    @MockitoBean
    protected NotificationRepository notificationRepository;

    @MockitoBean
    protected ProjectDailyMetricRepository projectDailyMetricRepository;

    @MockitoBean
    protected DashboardAiAnalysisRepository dashboardAiAnalysisRepository;

    @MockitoBean
    protected WeeklyInsightRepository weeklyInsightRepository;

    @MockitoBean
    protected TutorialRepository tutorialRepository;

    @MockitoBean
    protected CommunityBoardRepository communityBoardRepository;

    @MockitoBean
    protected CommunityPostRepository communityPostRepository;

    @MockitoBean
    protected CommunityPostCommentRepository communityPostCommentRepository;

    // RankingService(§4, 다른 담당자 구현)가 필요로 하는 뷰 전용 리포지토리도 같은 이유로 목으로 채운다.
    @MockitoBean
    protected ProjectRankingRepository projectRankingRepository;

    @MockitoBean
    protected DeveloperRankingRepository developerRankingRepository;
}
