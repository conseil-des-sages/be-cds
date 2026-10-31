package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.Participation;
import com.takima.backskeleton.models.Sage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ParticipationDao extends JpaRepository<Participation, Participation.Key> {
    /** Les sages d'une session, dans l'ordre de parole. */
    @Query("""
            select s from Participation p
            join Sage s on s.id = p.id.participantId
            where p.id.sessionId = :sessionId
              and s.kind = com.takima.backskeleton.models.SageKind.DEBATER
            order by p.seat
            """)
    List<Sage> findSages(@Param("sessionId") Integer sessionId);

    @Query("""
            select p.id.participantId as sageId, count(p) as uses from Participation p
            where p.id.participantId in :sageIds
            group by p.id.participantId
            """)
    List<SageUses> countUses(@Param("sageIds") Collection<Integer> sageIds);

    interface SageUses {
        Integer getSageId();

        long getUses();
    }
}
