package com.comutel.backend.workflow.repository;

import com.comutel.backend.workflow.model.WorkflowTransitionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkflowTransitionDefinitionRepository extends JpaRepository<WorkflowTransitionDefinition, Long> {
    List<WorkflowTransitionDefinition> findByWorkflowDefinitionIdAndFromStateKeyAndEventKeyAndActiveTrueOrderByPriorityAsc(
            Long workflowDefinitionId,
            String fromStateKey,
            String eventKey
    );

    List<WorkflowTransitionDefinition> findByWorkflowDefinitionIdAndFromStateKeyAndActiveTrueOrderByPriorityAsc(
            Long workflowDefinitionId,
            String fromStateKey
    );

    List<WorkflowTransitionDefinition> findByWorkflowDefinitionIdOrderByPriorityAsc(Long workflowDefinitionId);
}
