package com.comutel.backend.workflow.repository;

import com.comutel.backend.workflow.model.WorkflowInstanceTransitionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowInstanceTransitionLogRepository extends JpaRepository<WorkflowInstanceTransitionLog, Long> {
    List<WorkflowInstanceTransitionLog> findByWorkflowInstanceIdOrderByExecutedAtDesc(Long workflowInstanceId);
}
