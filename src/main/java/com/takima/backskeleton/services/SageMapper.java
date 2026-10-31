package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.ParticipationDao;
import com.takima.backskeleton.DTO.SageDtos;
import com.takima.backskeleton.models.Sage;
import com.takima.backskeleton.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SageMapper {
    private final ParticipationDao participationDao;

    public List<SageDtos.View> toViews(Collection<Sage> sages, Integer viewerId) {
        if (sages.isEmpty()) {
            return List.of();
        }
        List<Integer> ids = sages.stream().map(Sage::getId).toList();
        Map<Integer, Long> uses = participationDao.countUses(ids).stream()
                .collect(Collectors.toMap(ParticipationDao.SageUses::getSageId, ParticipationDao.SageUses::getUses));
        return sages.stream().map(sage -> toView(sage, viewerId, uses.getOrDefault(sage.getId(), 0L))).toList();
    }

    public SageDtos.View toView(Sage sage, Integer viewerId, long uses) {
        User creator = sage.getCreator();
        boolean mine = creator != null && creator.getId().equals(viewerId);
        String origin = creator == null ? "base" : mine ? "mine" : "library";
        return new SageDtos.View(
                sage.getId(),
                sage.getName(),
                sage.getShortName() == null ? sage.getName() : sage.getShortName(),
                sage.getSageDescription(),
                sage.getColor(),
                sage.getIcon(),
                origin,
                creator == null ? null : creator.getName(),
                sage.isShared(),
                sage.isInDefaultCouncil(),
                uses,
                mine ? sage.getSageSystemPrompt() : null,
                mine ? sage.getSageModel() : null,
                mine ? sage.getSageTemperature() : null,
                mine ? sage.getSageMaxTokens() : null);
    }
}
