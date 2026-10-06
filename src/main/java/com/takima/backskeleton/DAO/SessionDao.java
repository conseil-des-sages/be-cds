package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionDao extends JpaRepository<Session, Long> {
}
