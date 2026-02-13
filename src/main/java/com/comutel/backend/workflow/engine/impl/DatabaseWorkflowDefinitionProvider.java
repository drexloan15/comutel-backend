package com.comutel.backend.workflow.engine.impl;

import com.comutel.backend.workflow.engine.WorkflowDefinitionProvider;
import com.comutel.backend.workflow.model.WorkflowDefinition;
import com.comutel.backend.workflow.repository.WorkflowDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseWorkflowDefinitionProvider implements WorkflowDefinitionProvider {

    @Autowired
    private WorkflowDefinitionRepository workflowDefinitionRepository;

    @Override
    public WorkflowDefinition loadActive(String workflowKey, String processType) {
        return workflowDefinitionRepository
                .findFirstByKeyAndProcessTypeAndActiveTrueOrderByVersionDesc(workflowKey, processType)
                .orElseThrow(() -> new RuntimeException("No existe workflow activo para key=" + workflowKey + " processType=" + processType));
    }

    @Override
    public WorkflowDefinition loadById(Long id) {
        return workflowDefinitionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WorkflowDefinition no encontrado: " + id));
    }
}
