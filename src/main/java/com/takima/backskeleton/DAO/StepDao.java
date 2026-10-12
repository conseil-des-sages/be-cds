package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.Step;
import com.takima.backskeleton.models.StepStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StepDao extends JpaRepository<Step, Integer> {
    List<Step> findBySession_SessionIdOrderByOrderAsc(Integer sessionId);
    boolean existsBySession_SessionIdAndOrder(Integer sessionId, Integer order);
    boolean existsBySession_SessionIdAndStatus(Integer sessionId, StepStatus stepStatus);
}
