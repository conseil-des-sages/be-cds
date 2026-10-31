package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.Step;
import com.takima.backskeleton.models.StepStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StepDao extends JpaRepository<Step, Integer> {
    List<Step> findBySession_SessionIdOrderByOrderAsc(Integer sessionId);
    boolean existsBySession_SessionIdAndOrder(Integer sessionId, Integer order);
    boolean existsBySession_SessionIdAndStatus(Integer sessionId, StepStatus stepStatus);

    /** Verrouille l'étape le temps d'y ranger un message : deux messages ne prennent jamais le même rang. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Step s where s.id = :id")
    Optional<Step> findByIdForUpdate(@Param("id") Integer id);
}
