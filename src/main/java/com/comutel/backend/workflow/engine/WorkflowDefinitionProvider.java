package com.comutel.backend.workflow.engine;

import com.comutel.backend.workflow.model.WorkflowDefinition;

public interface WorkflowDefinitionProvider {
    WorkflowDefinition loadActive(String workflowKey, String processType);
    WorkflowDefinition loadById(Long id);
}
