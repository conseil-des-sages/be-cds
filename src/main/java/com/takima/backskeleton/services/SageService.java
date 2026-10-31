package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.CouncilSeatDao;
import com.takima.backskeleton.DAO.SageDao;
import com.takima.backskeleton.DAO.UserDao;
import com.takima.backskeleton.DTO.SageDtos;
import com.takima.backskeleton.models.Sage;
import com.takima.backskeleton.models.User;
import com.takima.backskeleton.services.llm.LlmRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SageService {
    private static final Pattern ARTICLE = Pattern.compile("^(Le |La |Les |L'|L’)");

    private final SageDao sageDao;
    private final UserDao userDao;
    private final CouncilSeatDao councilSeatDao;
    private final SageMapper sageMapper;
    private final LlmRouter llmRouter;

    @Transactional(readOnly = true)
    public List<SageDtos.View> findVisible(Integer userId) {
        return sageMapper.toViews(sageDao.findVisibleTo(userId), userId);
    }

    @Transactional
    public SageDtos.View create(Integer userId, SageDtos.UpsertRequest request) {
        User creator = userDao.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Compte introuvable."));
        Sage sage = new Sage();
        sage.setCreator(creator);
        apply(sage, request);
        return sageMapper.toView(sageDao.saveAndFlush(sage), userId, 0);
    }

    @Transactional
    public SageDtos.View update(Integer userId, Integer sageId, SageDtos.UpsertRequest request) {
        Sage sage = requireOwned(userId, sageId);
        apply(sage, request);
        return sageMapper.toViews(List.of(sage), userId).get(0);
    }

    /** Suppression logique : les débats passés gardent les interventions de ce sage. */
    @Transactional
    public void delete(Integer userId, Integer sageId) {
        Sage sage = requireOwned(userId, sageId);
        sage.setDeletedAt(LocalDateTime.now());
        councilSeatDao.deleteBySage(sageId);
    }

    private void apply(Sage sage, SageDtos.UpsertRequest request) {
        if (!llmRouter.isKnownModel(request.model())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce modèle n'est pas proposé.");
        }
        String name = request.name().trim();
        sage.setName(name);
        sage.setShortName(ARTICLE.matcher(name).replaceFirst(""));
        sage.setSageDescription(request.description().trim());
        sage.setSageSystemPrompt(request.prompt().trim());
        sage.setSageModel(request.model());
        sage.setSageTemperature(request.temperature());
        sage.setSageMaxTokens(request.maxTokens());
        sage.setColor(request.color());
        sage.setIcon(request.icon());
        sage.setShared(request.shared());
    }

    private Sage requireOwned(Integer userId, Integer sageId) {
        return sageDao.findById(sageId)
                .filter(sage -> sage.getDeletedAt() == null)
                .filter(sage -> sage.getCreator() != null && sage.getCreator().getId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sage introuvable."));
    }
}
