package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SessionDao extends JpaRepository<Session, Integer> {
    List<Session> findByOwner_IdOrderBySessionCreatedAtDesc(Integer ownerId);

    long countByOwner_Id(Integer ownerId);

    long countByOwner_IdAndSessionCreatedAtAfter(Integer ownerId, LocalDateTime after);

    long countBySessionCreatedAtAfter(LocalDateTime after);

    @Query("""
            select s.sessionTheme as theme, count(s) as debates from Session s
            where s.sessionTheme is not null and s.sessionTheme <> :excluded
            group by s.sessionTheme
            order by count(s) desc
            """)
    List<ThemeCount> countThemes(@Param("excluded") String excluded);

    interface ThemeCount {
        String getTheme();

        long getDebates();
    }
}
