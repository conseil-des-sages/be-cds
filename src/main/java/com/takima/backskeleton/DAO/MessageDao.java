package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageDao extends JpaRepository<Message, Integer> {
    @Query("""
            select m from Message m
            join fetch m.step s
            join fetch m.participant
            left join fetch m.parent
            where s.session.sessionId = :sessionId and m.deletedAt is null
            order by s.order, m.position
            """)
    List<Message> findBySession(@Param("sessionId") Integer sessionId);

    @Query("select coalesce(max(m.position), 0) from Message m where m.step.id = :stepId")
    int findLastPosition(@Param("stepId") Integer stepId);
}
