package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.SageDao;
import com.takima.backskeleton.models.Sage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Ce qui fait un conseil valable : assez de voix, sans doublon, et uniquement des sages accessibles. */
@Component
@RequiredArgsConstructor
public class CouncilRules {
    private final SageDao sageDao;

    @Value("${app.debate.min-sages}")
    private int minSages;

    @Value("${app.debate.max-sages}")
    private int maxSages;

    public int minSages() {
        return minSages;
    }

    public int maxSages() {
        return maxSages;
    }

    public List<Sage> resolve(Integer userId, List<Integer> sageIds) {
        if (sageIds.size() < minSages || sageIds.size() > maxSages) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un conseil réunit entre " + minSages + " et " + maxSages + " sages.");
        }
        if (new HashSet<>(sageIds).size() != sageIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Un sage ne siège qu'une fois.");
        }
        Map<Integer, Sage> visible = sageDao.findVisibleTo(userId).stream()
                .collect(Collectors.toMap(Sage::getId, Function.identity()));
        return sageIds.stream()
                .map(id -> {
                    Sage sage = visible.get(id);
                    if (sage == null) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce sage n'est pas disponible.");
                    }
                    return sage;
                })
                .toList();
    }
}
