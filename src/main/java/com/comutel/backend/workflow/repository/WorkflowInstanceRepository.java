package com.comutel.backend.workflow.repository;

import com.comutel.backend.workflow.model.WorkflowInstance;
import com.comutel.backend.workflow.model.WorkflowInstanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
    Optional<WorkflowInstance> findByEntityTypeAndEntityId(String entityType, Long entityId);
    List<WorkflowInstance> findByStatusAndDueAtBefore(WorkflowInstanceStatus status, LocalDateTime dueAt);
}
