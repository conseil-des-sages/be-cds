package com.takima.backskeleton.DAO;

import com.takima.backskeleton.models.Step;
import com.takima.backskeleton.models.StepStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StepDao extends JpaRepository<Step, Integer> {
    List<Step> findBySessionIdOrderByStepOrderAsc(Integer sessionId);
    boolean existsBySessionIdOrderByStepOrderAsc(Integer sessionId);
    boolean existsBySessionIdAndStepOrderAsc(Integer sessionId, Integer stepOrder);
    boolean existsBySessionIdAndStepOrder(Long sessionId, Integer stepOrder);
    boolean existsBySessionIdAndStatus(Long sessionId, StepStatus stepStatus);
}
