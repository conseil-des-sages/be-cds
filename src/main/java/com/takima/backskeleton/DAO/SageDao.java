package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.Sage;
import com.takima.backskeleton.models.SageKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SageDao extends JpaRepository<Sage, Integer> {
    /** Ce qu'un joueur peut choisir : les sages de base, les siens et ceux partagés par les autres. */
    @Query("""
            select s from Sage s
            left join fetch s.creator c
            where s.deletedAt is null
              and s.kind = com.takima.backskeleton.models.SageKind.DEBATER
              and (c is null or c.id = :userId or s.shared = true)
            order by s.id
            """)
    List<Sage> findVisibleTo(@Param("userId") Integer userId);

    @Query("""
            select s from Sage s
            where s.deletedAt is null and s.inDefaultCouncil = true
            order by s.id
            """)
    List<Sage> findDefaultCouncil();

    Optional<Sage> findFirstByKindAndDeletedAtIsNull(SageKind kind);

    long countByCreator_IdAndDeletedAtIsNull(Integer creatorId);
}
