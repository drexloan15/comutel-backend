package com.comutel.backend.workflow.engine;

import com.comutel.backend.workflow.engine.dto.Assignment;
import com.comutel.backend.workflow.engine.dto.RuleContext;
import com.comutel.backend.workflow.model.WorkflowStateDefinition;
import com.comutel.backend.workflow.model.WorkflowInstance;

public interface AssignmentResolver {
    Assignment resolve(WorkflowInstance instance, WorkflowStateDefinition targetState, RuleContext context);
}
