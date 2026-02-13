package com.comutel.backend.workflow.actions;

import com.comutel.backend.workflow.engine.dto.RuleContext;
import com.comutel.backend.workflow.model.WorkflowTransitionAction;

public interface WorkflowActionHandler {
    boolean supports(String actionKey);
    void execute(WorkflowTransitionAction action, RuleContext context);
}
