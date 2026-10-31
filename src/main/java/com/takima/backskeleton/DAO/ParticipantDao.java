package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.Participant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantDao extends JpaRepository<Participant, Integer> {
}
