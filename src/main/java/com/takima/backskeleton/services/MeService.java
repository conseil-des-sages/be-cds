package com.takima.backskeleton.services;

import com.takima.backskeleton.DAO.CouncilSeatDao;
import com.takima.backskeleton.DAO.SageDao;
import com.takima.backskeleton.DAO.SessionDao;
import com.takima.backskeleton.DAO.UserDao;
import com.takima.backskeleton.DTO.MeDtos;
import com.takima.backskeleton.DTO.PatchUserDto;
import com.takima.backskeleton.models.CouncilSeat;
import com.takima.backskeleton.models.Sage;
import com.takima.backskeleton.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeService {
    private final UserDao userDao;
    private final SessionDao sessionDao;
    private final SageDao sageDao;
    private final CouncilSeatDao councilSeatDao;
    private final CouncilRules councilRules;

    @Transactional(readOnly = true)
    public MeDtos.Profile profile(Integer userId) {
        return toProfile(requireUser(userId));
    }

    @Transactional
    public MeDtos.Profile rename(Integer userId, PatchUserDto request) {
        User user = requireUser(userId);
        user.setName(request.getName().trim());
        return toProfile(user);
    }

    /** Le conseil choisi par le joueur, ou le conseil par défaut tant qu'il n'en a pas composé. */
    @Transactional(readOnly = true)
    public MeDtos.CouncilView council(Integer userId) {
        List<Integer> ids = councilSeatDao.findCouncil(userId).stream()
                .map(seat -> seat.getSage().getId())
                .toList();
        if (ids.size() < councilRules.minSages()) {
            ids = sageDao.findDefaultCouncil().stream().map(Sage::getId).toList();
        }
        return new MeDtos.CouncilView(ids, councilRules.minSages(), councilRules.maxSages());
    }

    @Transactional
    public MeDtos.CouncilView saveCouncil(Integer userId, MeDtos.CouncilRequest request) {
        List<Sage> sages = councilRules.resolve(userId, request.sageIds());
        User user = requireUser(userId);
        councilSeatDao.deleteCouncil(userId);
        for (int seat = 0; seat < sages.size(); seat++) {
            councilSeatDao.save(new CouncilSeat(user, sages.get(seat), seat));
        }
        return new MeDtos.CouncilView(request.sageIds(), councilRules.minSages(), councilRules.maxSages());
    }

    private MeDtos.Profile toProfile(User user) {
        return new MeDtos.Profile(
                user.getId(),
                user.getName(),
                user.getEmailUser(),
                user.getRole() == null ? null : user.getRole().getRoleName(),
                user.getCreatedAt(),
                sessionDao.countByOwner_Id(user.getId()),
                sageDao.countByCreator_IdAndDeletedAtIsNull(user.getId()));
    }

    private User requireUser(Integer userId) {
        return userDao.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Compte introuvable."));
    }
}
