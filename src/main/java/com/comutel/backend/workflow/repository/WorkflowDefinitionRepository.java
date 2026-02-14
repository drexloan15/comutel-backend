package com.comutel.backend.workflow.repository;

import com.comutel.backend.workflow.model.WorkflowDefinition;
import com.comutel.backend.workflow.model.WorkflowDefinitionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    Optional<WorkflowDefinition> findFirstByKeyAndProcessTypeAndActiveTrueOrderByVersionDesc(String key, String processType);
    Optional<WorkflowDefinition> findFirstByProcessTypeAndActiveTrueOrderByVersionDesc(String processType);
    Optional<WorkflowDefinition> findFirstByKeyAndActiveTrueOrderByVersionDesc(String key);
    Optional<WorkflowDefinition> findFirstByActiveTrueOrderByVersionDesc();
    boolean existsByKeyAndProcessType(String key, String processType);
    List<WorkflowDefinition> findByProcessTypeOrderByKeyAscVersionDesc(String processType);
    List<WorkflowDefinition> findByStatus(WorkflowDefinitionStatus status);
}
