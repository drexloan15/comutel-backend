package com.comutel.backend.workflow.repository;

import com.comutel.backend.workflow.model.WorkflowAssignmentRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowAssignmentRuleRepository extends JpaRepository<WorkflowAssignmentRule, Long> {
    List<WorkflowAssignmentRule> findByWorkflowDefinitionIdAndStateKeyAndActiveTrueOrderByPriorityOrderAsc(Long workflowDefinitionId, String stateKey);
    List<WorkflowAssignmentRule> findByWorkflowDefinitionIdOrderByPriorityOrderAsc(Long workflowDefinitionId);
}
