package com.comutel.backend.workflow.actions.impl;

import com.comutel.backend.workflow.actions.WorkflowActionHandler;
import com.comutel.backend.workflow.engine.dto.RuleContext;
import com.comutel.backend.workflow.model.WorkflowTransitionAction;
import com.comutel.backend.workflow.service.WorkflowPayloadCodec;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SetPayloadActionHandler implements WorkflowActionHandler {

    @Override
    public boolean supports(String actionKey) {
        return "SET_PAYLOAD".equalsIgnoreCase(actionKey);
    }

    @Override
    public void execute(WorkflowTransitionAction action, RuleContext context) {
        Map<String, String> params = WorkflowPayloadCodec.decode(action.getActionParams());
        String key = params.get("key");
        String value = params.get("value");
        if (key != null && value != null) {
            context.getPayload().put(key, value);
        }
    }
}
