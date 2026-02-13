package com.comutel.backend.workflow.actions.impl;

import com.comutel.backend.workflow.actions.WorkflowActionHandler;
import com.comutel.backend.workflow.engine.dto.RuleContext;
import com.comutel.backend.workflow.model.WorkflowOutboxEvent;
import com.comutel.backend.workflow.model.WorkflowOutboxStatus;
import com.comutel.backend.workflow.model.WorkflowTransitionAction;
import com.comutel.backend.workflow.repository.WorkflowOutboxEventRepository;
import com.comutel.backend.workflow.service.WorkflowPayloadCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class EmitEventActionHandler implements WorkflowActionHandler {

    @Autowired
    private WorkflowOutboxEventRepository outboxEventRepository;

    @Override
    public boolean supports(String actionKey) {
        return "EMIT_EVENT".equalsIgnoreCase(actionKey);
    }

    @Override
    public void execute(WorkflowTransitionAction action, RuleContext context) {
        Map<String, String> params = WorkflowPayloadCodec.decode(action.getActionParams());

        WorkflowOutboxEvent event = new WorkflowOutboxEvent();
        event.setEventType(params.getOrDefault("eventType", "WORKFLOW.CUSTOM"));
        event.setAggregateType(context.getInstance().getEntityType());
        event.setAggregateId(context.getInstance().getEntityId());
        event.setPayload(WorkflowPayloadCodec.encode(context.getPayload()));
        event.setStatus(WorkflowOutboxStatus.PENDING);
        event.setAvailableAt(LocalDateTime.now());
        outboxEventRepository.save(event);
    }
}
