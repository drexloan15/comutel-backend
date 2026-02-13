package com.comutel.backend.workflow.actions;

import com.comutel.backend.workflow.engine.dto.RuleContext;
import com.comutel.backend.workflow.model.WorkflowTransitionAction;

import java.util.List;

public interface ActionDispatcher {
    void dispatch(List<WorkflowTransitionAction> actions, RuleContext context);
}
