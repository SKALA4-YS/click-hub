package com.skala.clickhub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.skala.clickhub.entity.ActorKind;
import com.skala.clickhub.entity.AnonymousSession;
import com.skala.clickhub.entity.EventType;
import com.skala.clickhub.entity.InteractionEvent;
import com.skala.clickhub.entity.Project;
import com.skala.clickhub.repository.AnonymousSessionRepository;
import com.skala.clickhub.repository.InteractionEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * interaction_events 기록 공용 진입점.
 *
 * actor_key는 NOT NULL이고 actor_kind로 로그인/익명을 구분한다(폴리모픽 액터).
 * 비로그인 사용자는 anonymous_sessions 행이 있어야 키를 만들 수 있는데, 아직 세션 쿠키를
 * 내려주는 흐름이 없어서 지금은 이벤트마다 새 익명 세션을 만든다.
 * TODO: 프론트에 세션 쿠키를 내려주고 재사용해야 "고유 방문자" 집계(unique actor_key)가
 *       의미를 갖는다 — 현재 구조에서는 익명 방문이 전부 서로 다른 사람으로 집계된다.
 */
@Service
@RequiredArgsConstructor
public class InteractionEventRecorder {

    // 앱 전역 ObjectMapper 빈을 주입받지 않고 전용 인스턴스를 쓴다 — nodb 프로필처럼 일부 자동 구성이
    // 빠진 테스트 컨텍스트에서 ObjectMapper 빈을 못 찾는 경우가 있었고(SecurityConfig 주석 참고),
    // 여기서 만드는 값은 빈 JSON 객체뿐이라 앱의 커스텀 직렬화 설정과 무관하다.
    private static final ObjectMapper JSON = new ObjectMapper();

    private final InteractionEventRepository interactionEventRepository;
    private final AnonymousSessionRepository anonymousSessionRepository;

    @Transactional
    public void record(EventType eventType, Project project, UUID userIdOrNull) {
        record(eventType, project, userIdOrNull, JSON.createObjectNode());
    }

    @Transactional
    public void recordToggle(EventType eventType, Project project, UUID userId, boolean enabled) {
        ObjectNode context = JSON.createObjectNode();
        context.put("enabled", enabled);
        record(eventType, project, userId, context);
    }

    private void record(EventType eventType, Project project, UUID userIdOrNull, ObjectNode context) {
        ActorKind actorKind = userIdOrNull != null ? ActorKind.USER : ActorKind.ANONYMOUS;
        UUID actorKey = userIdOrNull != null ? userIdOrNull : newAnonymousActorKey();

        interactionEventRepository.save(InteractionEvent.builder()
                .eventType(eventType)
                .actorKind(actorKind)
                .actorKey(actorKey)
                .project(project)
                .context(context)
                .build());
    }

    private UUID newAnonymousActorKey() {
        AnonymousSession session = anonymousSessionRepository.save(AnonymousSession.builder().build());
        return session.getId();
    }
}
