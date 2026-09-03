package com.skala.clickhub.service;

import com.skala.clickhub.dto.ranking.RankingDtos.DeveloperRankingItem;
import com.skala.clickhub.dto.ranking.RankingDtos.ProjectRankingItem;
import com.skala.clickhub.entity.DeveloperRankingView;
import com.skala.clickhub.entity.ProjectRankingView;
import com.skala.clickhub.repository.DeveloperRankingRepository;
import com.skala.clickhub.repository.ProjectRankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final ProjectRankingRepository projectRankingRepository;
    private final DeveloperRankingRepository developerRankingRepository;

    public List<ProjectRankingItem> getProjectRanking() {
        List<ProjectRankingView> views = projectRankingRepository.findTop100ByOrderByScoreDesc();
        return toRankedList(views, (view, rank) ->
                new ProjectRankingItem(rank, view.getProjectId(), view.getTitle(), view.getScore()));
    }

    public List<DeveloperRankingItem> getDeveloperRanking() {
        List<DeveloperRankingView> views = developerRankingRepository.findTop100ByOrderByScoreDesc();
        return toRankedList(views, (view, rank) ->
                new DeveloperRankingItem(rank, view.getCreatorId(), view.getDisplayName(), view.getScore()));
    }

    private <V, D> List<D> toRankedList(List<V> views, RankMapper<V, D> mapper) {
        return java.util.stream.IntStream.range(0, views.size())
                .mapToObj(i -> mapper.map(views.get(i), i + 1))
                .toList();
    }

    @FunctionalInterface
    private interface RankMapper<V, D> {
        D map(V view, int rank);
    }
}
