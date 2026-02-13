package com.comutel.backend.workflow.repository;

import com.comutel.backend.workflow.model.WorkflowTransitionAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowTransitionActionRepository extends JpaRepository<WorkflowTransitionAction, Long> {
    List<WorkflowTransitionAction> findByTransitionIdOrderByOrderNoAsc(Long transitionId);
}
