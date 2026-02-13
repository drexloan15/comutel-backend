package com.comutel.backend.workflow.repository;

import com.comutel.backend.workflow.model.WorkflowOutboxEvent;
import com.comutel.backend.workflow.model.WorkflowOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkflowOutboxEventRepository extends JpaRepository<WorkflowOutboxEvent, Long> {
    List<WorkflowOutboxEvent> findTop100ByStatusAndAvailableAtBeforeOrderByIdAsc(WorkflowOutboxStatus status, LocalDateTime availableAt);
}
