package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.ScribeNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScribeNoteDao extends JpaRepository<ScribeNote, Integer> {
    List<ScribeNote> findBySession_SessionIdOrderByAfterTurnsAsc(Integer sessionId);
}
