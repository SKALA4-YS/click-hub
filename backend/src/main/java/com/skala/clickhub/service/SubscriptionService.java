package com.skala.clickhub.service;

import com.skala.clickhub.dto.subscribe.SubscribeDtos.SubscriptionResponse;
import com.skala.clickhub.entity.CreatorSubscription;
import com.skala.clickhub.entity.User;
import com.skala.clickhub.entity.id.CreatorSubscriptionId;
import com.skala.clickhub.exception.BusinessException;
import com.skala.clickhub.exception.ErrorCode;
import com.skala.clickhub.repository.CreatorSubscriptionRepository;
import com.skala.clickhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** 기획서 7장 — 사용자는 프로젝트가 아니라 "제작자"를 구독한다. */
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final CreatorSubscriptionRepository creatorSubscriptionRepository;
    private final UserRepository userRepository;

    @Transactional
    public SubscriptionResponse toggle(UUID subscriberId, UUID creatorId) {
        if (subscriberId.equals(creatorId)) {
            throw new BusinessException(ErrorCode.CANNOT_SUBSCRIBE_SELF);
        }

        CreatorSubscriptionId id = new CreatorSubscriptionId(subscriberId, creatorId);
        if (creatorSubscriptionRepository.existsById(id)) {
            creatorSubscriptionRepository.deleteById(id);
            return new SubscriptionResponse(false);
        }

        User subscriber = userRepository.findById(subscriberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        creatorSubscriptionRepository.save(CreatorSubscription.builder()
                .subscriber(subscriber)
                .creator(creator)
                .build());

        return new SubscriptionResponse(true);
    }
}
