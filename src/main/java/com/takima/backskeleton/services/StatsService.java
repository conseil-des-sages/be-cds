package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.SessionDao;
import com.takima.backskeleton.DTO.StatsDto;
import com.takima.backskeleton.services.debate.SynthesisParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsService {
    private static final int MAX_THEMES = 3;

    private final SessionDao sessionDao;

    @Transactional(readOnly = true)
    public StatsDto stats() {
        List<String> themes = sessionDao.countThemes(SynthesisParser.OTHER_THEME).stream()
                .limit(MAX_THEMES)
                .map(SessionDao.ThemeCount::getTheme)
                .toList();
        return new StatsDto(
                sessionDao.count(),
                sessionDao.countBySessionCreatedAtAfter(LocalDateTime.now().minusWeeks(1)),
                themes);
    }
}
