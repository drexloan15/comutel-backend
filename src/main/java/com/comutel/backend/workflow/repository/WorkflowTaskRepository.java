package com.comutel.backend.workflow.repository;

import com.comutel.backend.workflow.model.WorkflowTask;
import com.comutel.backend.workflow.model.WorkflowTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {
    List<WorkflowTask> findByWorkflowInstanceIdOrderByCreatedAtDesc(Long workflowInstanceId);
    List<WorkflowTask> findByAssigneeUserIdAndStatus(Long assigneeUserId, WorkflowTaskStatus status);
    List<WorkflowTask> findByAssigneeGroupIdAndStatus(Long assigneeGroupId, WorkflowTaskStatus status);
}
